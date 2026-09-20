package coin.exchange.module.datasrouce.adapter;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.api.market.model.MarketSymbolVo;
import coin.exchange.api.market.service.RemoteMarketService;
import coin.exchange.common.core.response.R;
import coin.exchange.module.datasrouce.cache.MarketMemoryCache;
import coin.exchange.module.datasrouce.config.BinanceProperties;
import coin.exchange.module.datasrouce.domain.DepthWsMessageDo;
import coin.exchange.module.datasrouce.domain.KlineWsMessageDo;
import coin.exchange.module.datasrouce.domain.TickerWsMessageDo;
import coin.exchange.module.datasrouce.domain.TradeWsMessageDo;
import coin.exchange.module.datasrouce.enums.BinanceStreamType;
import coin.exchange.module.datasrouce.mq.MarketDataPublisher;
import coin.exchange.module.datasrouce.utils.BinanceUtils;
import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceWsClient {

    private static final int MARKET_WORKER_COUNT = 4;
    private static final int MARKET_QUEUE_CAPACITY = 2000;

    private final BinanceProperties binanceProperties;
    private final RemoteMarketService remoteMarketService;
    private final MarketMemoryCache marketMemoryCache;
    private final MarketDataPublisher marketDataPublisher;

    private final List<Integer> connectionIds = new CopyOnWriteArrayList<>();
    private final AtomicLong discardedMarketTasks = new AtomicLong();
    private final ThreadPoolExecutor marketExecutor = new ThreadPoolExecutor(
            MARKET_WORKER_COUNT,
            MARKET_WORKER_COUNT,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MARKET_QUEUE_CAPACITY),
            runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("binance-market-" + thread.getId());
                thread.setDaemon(true);
                return thread;
            },
            (task, executor) -> {
                if (executor.isShutdown()) {
                    return;
                }
                executor.getQueue().poll();
                executor.getQueue().offer(task);
                long discarded = discardedMarketTasks.incrementAndGet();
                if (discarded == 1 || discarded % 1000 == 0) {
                    log.warn("Binance行情队列已满，丢弃旧任务并保留最新数据: discarded={}, queueSize={}",
                            discarded, executor.getQueue().size());
                }
            }
    );
    private final ScheduledExecutorService subscriptionRetryExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("binance-subscription-retry");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean subscriptionRetryScheduled = new AtomicBoolean(false);
    private final AtomicBoolean marketStreamsSubscribed = new AtomicBoolean(false);
    private final AtomicLong activeConnectionGeneration = new AtomicLong();
    private volatile Set<String> subscribedSymbols = Set.of();
    private volatile WebSocketStreamClient client;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知字段

    public void start() {
        String baseUrl = binanceProperties.getBaseUrl();
        client = new WebSocketStreamClientImpl(baseUrl);
        running.set(true);
        subscribeMarketStreamsWhenAvailable();
    }

    private void subscribeMarketStreamsWhenAvailable() {
        if (!running.get() || marketStreamsSubscribed.get()) {
            return;
        }

        try {
            List<String> symbols = loadMarketSymbols();
            if (symbols.isEmpty()) {
                scheduleSubscriptionRetry();
                return;
            }
            Integer connectionId = subscribeCombineStreams(
                    binanceProperties.getKlineInterval(),
                    binanceProperties.getStreamTypes(),
                    symbols
            );
            if (connectionId != null) {
                marketStreamsSubscribed.set(true);
                subscribedSymbols = Set.copyOf(symbols);
            } else {
                scheduleSubscriptionRetry();
            }
        } catch (Exception e) {
            log.warn("market 服务暂不可用，datasource 保持运行并稍后重试: {}", e.getMessage());
            scheduleSubscriptionRetry();
        }
    }

    /** 定期同步 market_symbol，交易对启停后自动重建 Binance 订阅。 */
    @Scheduled(initialDelayString = "${exchange.market.binance.symbol-refresh-ms:30000}",
            fixedDelayString = "${exchange.market.binance.symbol-refresh-ms:30000}")
    public synchronized void refreshMarketSymbolSubscriptions() {
        if (!running.get() || !marketStreamsSubscribed.get()) {
            return;
        }
        List<String> symbols = loadMarketSymbols();
        if (symbols.isEmpty() || subscribedSymbols.equals(Set.copyOf(symbols))) {
            return;
        }

        log.info("market 交易对发生变化，重建 Binance 订阅: old={}, new={}", subscribedSymbols, symbols);
        marketStreamsSubscribed.set(false);
        WebSocketStreamClient currentClient = client;
        if (currentClient != null) {
            connectionIds.forEach(connectionId -> {
                try {
                    currentClient.closeConnection(connectionId);
                } catch (Exception e) {
                    log.debug("关闭旧 Binance 订阅失败: connectionId={}", connectionId, e);
                }
            });
        }
        connectionIds.clear();
        subscribedSymbols = Set.of();
        subscribeMarketStreamsWhenAvailable();
    }

    private void scheduleSubscriptionRetry() {
        if (!running.get() || !subscriptionRetryScheduled.compareAndSet(false, true)) {
            return;
        }

        long delayMillis = Math.max(1000L, binanceProperties.getReconnectDelay().toMillis());
        log.info("将在 {} ms 后重新获取交易对并尝试订阅", delayMillis);
        subscriptionRetryExecutor.schedule(() -> {
            subscriptionRetryScheduled.set(false);
            subscribeMarketStreamsWhenAvailable();
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 订阅单币种的k线数据
     */
    public void subscribeKlineBySymbol(
            String symbol,
            String klineInterval
    ) {
        log.info("订阅单币 {} 的 {} K线数据", symbol, klineInterval);
        subscribeCombineStreams(klineInterval, List.of(BinanceStreamType.KLINE), List.of(symbol));
    }

    /**
     * 订阅单币种的trade数据
     */
    public void subscribeTrade(
            String symbol
    ) {
        log.info("订阅单币 {} 的交易数据", symbol);
        subscribeCombineStreams("1m", List.of(BinanceStreamType.TRADE), List.of(symbol));
    }

    /**
     * 订阅单币种的Ticker数据
     */
    public void subscribeTicker(
            String symbol
    ) {
        log.info("订阅单币 {} 的24小时行情数据", symbol);
        subscribeCombineStreams("1m", List.of(BinanceStreamType.TICKER), List.of(symbol));
    }

    /**
     * 订阅单币种的Depth数据
     */
    public void subscribeDepth(
            String symbol
    ) {
        log.info("订阅单币 {} 的深度数据", symbol);
        subscribeCombineStreams("1m", List.of(BinanceStreamType.DEPTH), List.of(symbol));
    }

    /**
     * 订阅多币种的k线数据
     */
    public void subscribeKlineBySymbols(
            List<String> symbols,
            String klineInterval
    ) {
        log.info("订阅【 {} 】的 {} K线数据", symbols.toString(), klineInterval);
        subscribeCombineStreams(klineInterval, List.of(BinanceStreamType.KLINE), symbols);
    }

    /**
     * 订阅全币种其他数据
     */
    public void subscribeQuoteAll() {
        List<String> symbols = loadMarketSymbols();
        subscribeCombineStreams("1m", List.of(BinanceStreamType.TICKER, BinanceStreamType.DEPTH, BinanceStreamType.TRADE), symbols);
    }

    /**
     * 订阅全币种的最新k线数据
     */
    public void subscribeKlineByAll() {
        List<String> symbols = loadMarketSymbols();
        subscribeCombineStreams("1m", List.of(BinanceStreamType.KLINE), symbols);
    }

    private List<String> loadMarketSymbols() {
        R<List<MarketSymbolVo>> response;
        try {
            response = remoteMarketService.listSymbols(1);
        } catch (Exception e) {
            log.warn("无法连接 market 服务获取交易对: {}", e.getMessage());
            return List.of();
        }
        if (response == null || response.code() != R.SUCCESS_CODE || response.data() == null) {
            String message = response == null ? "无响应" : response.message();
            log.error("从 market 服务获取交易对失败: {}", message);
            return List.of();
        }

        List<String> symbols = response.data().stream()
                .map(MarketSymbolVo::getSymbol)
                .filter(StringUtils::hasText)
                .map(symbol -> symbol.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        log.info("从 market 服务获取到 {} 个交易对: {}", symbols.size(), symbols);
        return symbols;
    }

    /**
     * 订阅流式数据
     */
    public Integer subscribeCombineStreams(
            String klineInterval,
            List<BinanceStreamType> streamTypes,
            List<String> symbols
    ) {
        if (symbols == null || symbols.isEmpty()) {
            log.warn("market 服务未返回交易对，跳过 Binance WebSocket 订阅");
            return null;
        }

        // 构建订阅流
        List<String> streams = BinanceUtils.buildStreams(streamTypes, symbols, klineInterval);
        if (streams.isEmpty()) {
            log.warn("Binance WebSocket 未配置订阅流，跳过启动");
            return null;
        }
        long connectionGeneration = activeConnectionGeneration.incrementAndGet();
        int connectionId = client.combineStreams(
                new ArrayList<>(streams),
                response -> log.info("Binance SDK WebSocket 已连接: {}", response.request().url()),
                this::onMessage,
                (code, reason) -> log.info("Binance SDK WebSocket 正在关闭: code={}, reason={}", code, reason),
                (code, reason) -> {
                    log.warn("Binance SDK WebSocket 已关闭: code={}, reason={}", code, reason);
                    reconnectMarketStreams(connectionGeneration);
                },
                (throwable, response) -> {
                    String responseText = response == null ? "null" : response.toString();
                    String errorMessage = throwable == null ? "unknown" : throwable.getMessage();
                    log.error("Binance SDK WebSocket 连接失败: response={}, error={}", responseText, errorMessage, throwable);
                    reconnectMarketStreams(connectionGeneration);
                }
        );
        connectionIds.add(connectionId);

        log.info("Binance SDK WebSocket 已启动，connectionId={}, streams={}", connectionId, streams);
        return connectionId;
    }

    /**
     * Binance 会因为网络波动或服务端断开连接。断线后必须清除已订阅标记，
     * 否则重试任务会误以为连接仍然有效，从而永久停止行情推送。
     */
    private void reconnectMarketStreams(long connectionGeneration) {
        if (!running.get() || connectionGeneration != activeConnectionGeneration.get()) {
            return;
        }
        marketStreamsSubscribed.set(false);
        subscribedSymbols = Set.of();
        scheduleSubscriptionRetry();
    }

    public void onMessage(String message) {
        if (!running.get()) {
            return;
        }
//        log.info("收到 Binance 行情数据: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode data = root.path("data");
            String eventType = data.path("e").asText();
            if (!StringUtils.hasText(eventType)) {
                log.warn("Binance WebSocket 消息缺少事件类型，message={}", message);
                return;
            }

            switch (eventType) {
                case "kline" -> cacheKline(message);
                case "24hrTicker" -> cacheTicker(message);
                case "depthUpdate" -> cacheDepth(message);
                case "trade" -> cacheTrade(message);
                default -> log.debug("忽略未处理的 Binance 事件类型: {}", eventType);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Binance message: {}", message, e);
        }
    }

    private void cacheKline(String message) throws JsonProcessingException {
        KlineWsMessageDo wsMsg = objectMapper.readValue(message, KlineWsMessageDo.class);
        KlineWsMessageDo.Source source = wsMsg.getData();
        KlineWsMessageDo.Source.Kline kline = source.getKline();
        BinanceKlineVo payload = toKlineVo(kline);
        submitMarketTask("kline", source.getS(), () -> {
            marketMemoryCache.putKline(source.getS(), kline.getI(), payload);
            marketDataPublisher.publish("kline", source.getS(), kline.getI(), payload);
        });
    }

    private void cacheTicker(String message) throws JsonProcessingException {
        TickerWsMessageDo wsMsg = objectMapper.readValue(message, TickerWsMessageDo.class);
        TickerWsMessageDo.Source ticker = wsMsg.getData();
        BinanceTickerVo payload = toTickerVo(ticker);
        submitMarketTask("ticker", ticker.getS(), () -> {
            marketMemoryCache.putTicker(ticker.getS(), payload);
            marketDataPublisher.publish("ticker", ticker.getS(), null, payload);
        });
    }

    private void cacheDepth(String message) throws JsonProcessingException {
        DepthWsMessageDo wsMsg = objectMapper.readValue(message, DepthWsMessageDo.class);
        DepthWsMessageDo.Source depth = wsMsg.getData();
        BinanceDepthVo payload = toDepthVo(depth);
        submitMarketTask("depth", depth.getS(), () -> {
            marketMemoryCache.putDepth(depth.getS(), payload);
            marketDataPublisher.publish("depth", depth.getS(), null, payload);
        });
    }

    private void cacheTrade(String message) throws JsonProcessingException {
        TradeWsMessageDo wsMsg = objectMapper.readValue(message, TradeWsMessageDo.class);
        TradeWsMessageDo.Source trade = wsMsg.getData();
        BinanceTradeVo payload = toTradeVo(trade);
        submitMarketTask("trade", trade.getS(), () -> {
            marketMemoryCache.putTrade(trade.getS(), payload);
            marketDataPublisher.publish("trade", trade.getS(), null, payload);
        });
    }

    private BinanceTickerVo toTickerVo(TickerWsMessageDo.Source source) {
        BinanceTickerVo target = new BinanceTickerVo();
        target.setSymbol(source.getS());
        target.setPriceChange(source.getP());
        target.setPriceChangePercent(source.getPriceChangePercent());
        target.setWeightedAvgPrice(source.getW());
        target.setPrevClosePrice(source.getX());
        target.setLastPrice(source.getC());
        target.setLastQuantity(source.getLastQuantity());
        target.setBidPrice(source.getB());
        target.setBidQuantity(source.getBestBidQuantity());
        target.setAskPrice(source.getA());
        target.setAskQuantity(source.getBestAskQuantity());
        target.setOpenPrice(source.getO());
        target.setHighPrice(source.getH());
        target.setLowPrice(source.getL());
        target.setVolume(source.getV());
        target.setQuoteVolume(source.getQ());
        target.setOpenTime(source.getOpenTime());
        target.setCloseTime(source.getCloseTime());
        target.setFirstTradeId(source.getFirstTradeId());
        target.setLastTradeId(source.getLastTradeId());
        target.setTradeCount(source.getN());
        return target;
    }

    private BinanceDepthVo toDepthVo(DepthWsMessageDo.Source source) {
        BinanceDepthVo target = new BinanceDepthVo();
        target.setLastUpdateId(source.getU());
        target.setBids(source.getBids());
        target.setAsks(source.getAsks());
        return target;
    }

    private BinanceTradeVo toTradeVo(TradeWsMessageDo.Source source) {
        BinanceTradeVo target = new BinanceTradeVo();
        target.setId(source.getT());
        target.setPrice(source.getP());
        target.setQuantity(source.getQ());
        target.setQuoteQuantity(source.getP().multiply(source.getQ()));
        target.setTime(source.getTradeTime());
        target.setBuyerMaker(source.getM());
        target.setBestMatch(source.getBestMatch());
        return target;
    }

    private BinanceKlineVo toKlineVo(KlineWsMessageDo.Source.Kline source) {
        BinanceKlineVo target = new BinanceKlineVo();
        target.setOpenTime(source.getT());
        target.setOpenPrice(source.getO());
        target.setHighPrice(source.getH());
        target.setLowPrice(source.getL());
        target.setClosePrice(source.getC());
        target.setVolume(source.getV());
        target.setCloseTime(source.getCloseTime());
        target.setQuoteAssetVolume(source.getQ());
        target.setTradeCount(source.getN());
        target.setTakerBuyBaseAssetVolume(source.getTakerBuyVolume());
        target.setTakerBuyQuoteAssetVolume(source.getTakerBuyTurnover());
        return target;
    }

    private void submitMarketTask(String type, String symbol, Runnable task) {
        if (!running.get()) {
            return;
        }
        try {
            marketExecutor.execute(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("处理Binance行情失败: type={}, symbol={}", type, symbol, e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.debug("Binance market executor stopped, skip {} {}", type, symbol);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("正在停止 Binance SDK WebSocket...");
        running.set(false);
        marketStreamsSubscribed.set(false);
        subscribedSymbols = Set.of();
        subscriptionRetryExecutor.shutdownNow();
        WebSocketStreamClient currentClient = client;
        if (currentClient != null) {
            connectionIds.forEach(currentClient::closeConnection);
            connectionIds.clear();
            client = null;
        }
        marketExecutor.shutdown();
        try {
            if (!marketExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                marketExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            marketExecutor.shutdownNow();
        }
        log.info("Binance SDK WebSocket 已停止");
    }
}
