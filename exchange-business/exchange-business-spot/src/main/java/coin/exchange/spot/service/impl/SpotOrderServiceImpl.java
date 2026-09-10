package coin.exchange.spot.service.impl;

import coin.exchange.api.account.dto.AccountFrozenAssetsDto;
import coin.exchange.api.account.service.RemoteAccountService;
import coin.exchange.api.market.model.WsKLineVo;
import coin.exchange.api.spot.dto.CreateSpotDto;
import coin.exchange.common.core.constant.RedisKeyConstants;
import coin.exchange.common.core.enums.AssociationTypeCode;
import coin.exchange.common.core.enums.WalletAssetsCode;
import coin.exchange.common.core.enums.WalletTypeCode;
import coin.exchange.common.core.response.R;
import coin.exchange.common.redis.service.RedisService;
import coin.exchange.spot.domain.SpotOrderDo;
import coin.exchange.spot.domain.SpotSymbolDo;
import coin.exchange.spot.domain.SpotTradeDo;
import coin.exchange.spot.mapper.SpotOrderMapper;
import coin.exchange.spot.mapper.SpotSymbolMapper;
import coin.exchange.spot.mapper.SpotTradeMapper;
import coin.exchange.spot.service.SpotOrderService;
import coin.exchange.spot.util.SpotIdGenerator;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpotOrderServiceImpl extends ServiceImpl<SpotOrderMapper, SpotOrderDo> implements SpotOrderService {

    @Value("${spot.fee_rate}")
    private BigDecimal feeRate;

    private static final int MAX_ORDER_ID_GENERATION_ATTEMPTS = 10;
    private static final int MAX_TRADE_ID_GENERATION_ATTEMPTS = 10;
    private static final int SIDE_BUY = 1;
    private static final int SIDE_SELL = 2;
    private static final String DEFAULT_KLINE_INTERVAL = "1m";

    private final RedisService redisService;
    private final SpotOrderMapper spotOrderMapper;
    private final SpotSymbolMapper spotSymbolMapper;
    private final SpotTradeMapper spotTradeMapper;
    private final RemoteAccountService remoteAccountService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(CreateSpotDto spotDto) {
        validateCreateRequest(spotDto);

        // 判断orderType类型
        SpotSymbolDo symbolConfig = getEnabledSymbol(spotDto.getSymbol());
        String orderSymbol = symbolConfig.getSymbol();
        Integer orderType = spotDto.getOrderType();

        // 创建订单
        SpotOrderDo order = createOrderFun(spotDto, symbolConfig);

        // 限价下单
        if (orderType == 1) {

            if (spotDto.getPrice() == null || spotDto.getPrice().signum() <= 0) {
                throw new IllegalArgumentException("限价下单必须提供大于0的委托价格");
            }

            // 1. 添加到委托挂单

            // 2. 冻结资产：买单冻结计价币成交额和预估手续费，卖单冻结基础币数量
            BigDecimal orderAmount = spotDto.getPrice().multiply(spotDto.getQuantity()).setScale(8, RoundingMode.DOWN);
            BigDecimal estimatedFee = orderAmount.multiply(feeRate).setScale(8, RoundingMode.DOWN);
            boolean buyOrder = order.getSide() == SIDE_BUY;
            AccountFrozenAssetsDto assetsDto = createAssetsDto(
                    spotDto,
                    order,
                    spotDto.getPrice(),
                    buyOrder ? order.getQuoteAsset() : order.getBaseAsset(),
                    buyOrder ? orderAmount.add(estimatedFee) : order.getOrderQuantity(),
                    WalletAssetsCode.SPOT_FROZEN_ASSETS
            );
            requireAccountOperationSuccess(remoteAccountService.frozenAssets(assetsDto), "冻结交易资产");

            // 3. 更新订单信息
            order.setFilledQuantity(BigDecimal.ZERO);
            order.setStatus(1); // 进入挂单，待成交
            spotOrderMapper.insert(order);
        }
        // 市价下单
        else if (orderType == 2) {

            // 1. 获取市场价格
            String klineKey = RedisKeyConstants.KLINE_KEY_PREFIX
                    + orderSymbol.toLowerCase(Locale.ROOT)
                    + ":"
                    + DEFAULT_KLINE_INTERVAL;
            WsKLineVo cacheSymbol = redisService.getCacheObject(klineKey);
            if (cacheSymbol == null || cacheSymbol.getC() == null || cacheSymbol.getC().signum() <= 0) {
                throw new IllegalStateException("无法获取有效的市场最新价格: " + orderSymbol);
            }
            BigDecimal lastPrice = cacheSymbol.getC(); // 最新K线收盘价
            spotDto.setPrice(lastPrice);
            order.setOrderPrice(lastPrice);

            // 2. 计算手续费
            BigDecimal amount = lastPrice.multiply(spotDto.getQuantity()).setScale(8, RoundingMode.DOWN); // 成交额
            BigDecimal feeAmount = amount.multiply(feeRate).setScale(8, RoundingMode.DOWN); // 手续费

            // 3. 更新订单信息
            order.setFilledQuantity(spotDto.getQuantity());
            order.setStatus(3); // 完全成交
            spotOrderMapper.insert(order);

            // 4. 扣除资产
            AccountFrozenAssetsDto assetsDto = createAssetsDto(
                    spotDto,
                    order,
                    lastPrice,
                    order.getSide() == SIDE_BUY ? order.getQuoteAsset() : order.getBaseAsset(),
                    order.getSide() == SIDE_BUY ? amount : order.getOrderQuantity(),
                    WalletAssetsCode.SPOT_DEDUCT_ASSETS
            );
            R<String> deductAssetsResult = remoteAccountService.deductAssets(assetsDto);
            requireAccountOperationSuccess(deductAssetsResult, "扣除交易资产");

            // 5. 扣除手续费
            AccountFrozenAssetsDto feeDto = createAssetsDto(
                    spotDto,
                    order,
                    lastPrice,
                    order.getQuoteAsset(),
                    feeAmount,
                    WalletAssetsCode.SPOT_DEDUCT_FEE
            );
            R<String> deductFeeResult = remoteAccountService.deductAssets(feeDto);
            requireAccountOperationSuccess(deductFeeResult, "扣除交易手续费");

            // 6. 创建交易历史
            SpotTradeDo trade = createTradeHistory(order, lastPrice, amount, feeAmount);
            if (spotTradeMapper.insert(trade) != 1) {
                throw new IllegalStateException("创建现货成交历史失败");
            }
        } else {
            throw new IllegalArgumentException("不支持的订单类型: " + orderType);
        }

        return order.getOrderId();
    }

    private static void validateCreateRequest(CreateSpotDto spotDto) {
        if (spotDto == null) {
            throw new IllegalArgumentException("下单参数不能为空");
        }
        if (spotDto.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (spotDto.getSymbol() == null || spotDto.getSymbol().isBlank()) {
            throw new IllegalArgumentException("交易对不能为空");
        }
        if (spotDto.getSide() == null || !Set.of(SIDE_BUY, SIDE_SELL).contains(spotDto.getSide())) {
            throw new IllegalArgumentException("买卖方向只允许1-买入或2-卖出");
        }
        if (spotDto.getOrderType() == null || !Set.of(1, 2).contains(spotDto.getOrderType())) {
            throw new IllegalArgumentException("订单类型只允许1-限价单或2-市价单");
        }
        if (spotDto.getQuantity() == null || spotDto.getQuantity().signum() <= 0) {
            throw new IllegalArgumentException("下单数量必须大于0");
        }
    }

    /**
     * 根据交易对读取服务端配置，客户端不能指定基础币种和计价币种。
     */
    private SpotSymbolDo getEnabledSymbol(String symbol) {
        String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);
        SpotSymbolDo symbolConfig = spotSymbolMapper.selectOne(
                Wrappers.<SpotSymbolDo>lambdaQuery()
                        .eq(SpotSymbolDo::getSymbol, normalizedSymbol)
                        .eq(SpotSymbolDo::getStatus, 1)
                        .last("limit 1")
        );
        if (symbolConfig == null) {
            throw new IllegalArgumentException("交易对不存在或未启用: " + normalizedSymbol);
        }
        if (symbolConfig.getBaseAsset() == null || symbolConfig.getBaseAsset().isBlank()
                || symbolConfig.getQuoteAsset() == null || symbolConfig.getQuoteAsset().isBlank()) {
            throw new IllegalStateException("交易对币种配置不完整: " + normalizedSymbol);
        }
        return symbolConfig;
    }

    /**
     * Feign fallback 也会返回 R.fail，必须显式检查，避免资产扣除失败后继续扣手续费。
     */
    private static void requireAccountOperationSuccess(R<String> result, String operation) {
        if (result == null || result.code() != R.SUCCESS_CODE) {
            String message = result == null ? "账户服务无响应" : result.message();
            throw new IllegalStateException(operation + "失败：" + message);
        }
    }

    // 构造冻结资产类
    private static AccountFrozenAssetsDto createAssetsDto(
            CreateSpotDto spotDto,
            SpotOrderDo order,
            BigDecimal lastPrice,
            String currency,
            BigDecimal quantity,
            WalletAssetsCode walletAssets
    ) {
        AccountFrozenAssetsDto assetsDto = new AccountFrozenAssetsDto();
        assetsDto.setUserId(spotDto.getUserId());
        assetsDto.setCurrency(currency);
        assetsDto.setPrice(lastPrice);
        assetsDto.setWalletType(WalletTypeCode.SPOT);
        assetsDto.setQuantity(quantity);
        assetsDto.setOperationType(walletAssets);
        assetsDto.setAssociationType(AssociationTypeCode.SPOT_TRADE.getCode());
        assetsDto.setAssociationId(order.getOrderId());
        return assetsDto;
    }

    private SpotTradeDo createTradeHistory(
            SpotOrderDo order,
            BigDecimal tradePrice,
            BigDecimal tradeAmount,
            BigDecimal feeAmount
    ) {
        SpotTradeDo trade = new SpotTradeDo();
        trade.setTradeId(generateUniqueTradeId());
        trade.setOrderId(order.getOrderId());
        trade.setUserId(order.getUserId());
        trade.setSymbol(order.getSymbol());
        trade.setBaseAsset(order.getBaseAsset());
        trade.setQuoteAsset(order.getQuoteAsset());
        trade.setSide(order.getSide());
        trade.setTradePrice(tradePrice);
        trade.setTradeQuantity(order.getFilledQuantity());
        trade.setTradeAmount(tradeAmount);
        trade.setFee(feeAmount);
        trade.setFeeAsset(order.getQuoteAsset());
        trade.setFeeUsdtValue(feeAmount);
        trade.setMarketPrice(tradePrice);
        trade.setTradeTime(LocalDateTime.now());
        return trade;
    }

    /**
     * 创建订单
     */
    private SpotOrderDo createOrderFun(CreateSpotDto spotDto, SpotSymbolDo symbolConfig) {
        SpotOrderDo spot = new SpotOrderDo();

        spot.setSymbol(symbolConfig.getSymbol());
        spot.setBaseAsset(symbolConfig.getBaseAsset());
        spot.setQuoteAsset(symbolConfig.getQuoteAsset());
        spot.setUserId(spotDto.getUserId());
        spot.setOrderType(spotDto.getOrderType());
        spot.setSide(spotDto.getSide());
        spot.setOrderQuantity(spotDto.getQuantity());
        spot.setOrderPrice(spotDto.getPrice());
        spot.setFilledQuantity(BigDecimal.ZERO);
        spot.setStatus(1);

        // 生成订单号
        spot.setOrderId(generateUniqueOrderId());

        return spot;
    }

    private String generateUniqueOrderId() {
        for (int attempt = 0; attempt < MAX_ORDER_ID_GENERATION_ATTEMPTS; attempt++) {
            String orderId = SpotIdGenerator.orderId();
            if (spotOrderMapper.selectById(orderId) == null) {
                return orderId;
            }
        }
        throw new IllegalStateException("生成现货订单号失败，请稍后重试");
    }

    private String generateUniqueTradeId() {
        for (int attempt = 0; attempt < MAX_TRADE_ID_GENERATION_ATTEMPTS; attempt++) {
            String tradeId = SpotIdGenerator.tradeId();
            if (spotTradeMapper.selectById(tradeId) == null) {
                return tradeId;
            }
        }
        throw new IllegalStateException("生成现货成交记录号失败，请稍后重试");
    }
}
