package coin.exchange.module.datasrouce.adapter;

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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceWsClient {

    private final BinanceProperties binanceProperties;
    private final RemoteMarketService remoteMarketService;
    private final MarketMemoryCache marketMemoryCache;
    private final MarketDataPublisher marketDataPublisher;

    private final List<Integer> connectionIds = new CopyOnWriteArrayList<>();
    private final ExecutorService marketExecutor = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("binance-market-" + thread.getId());
        thread.setDaemon(true);
        return thread;
    });
    private final ScheduledExecutorService subscriptionRetryExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("binance-subscription-retry");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean subscriptionRetryScheduled = new AtomicBoolean(false);
    private final AtomicBoolean marketStreamsSubscribed = new AtomicBoolean(false);
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
            } else {
                scheduleSubscriptionRetry();
            }
        } catch (Exception e) {
            log.warn("market 服务暂不可用，datasource 保持运行并稍后重试: {}", e.getMessage());
            scheduleSubscriptionRetry();
        }
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
            response = remoteMarketService.listSymbols();
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
        int connectionId = client.combineStreams(
                new ArrayList<>(streams),
                response -> log.info("Binance SDK WebSocket 已连接: {}", response.request().url()),
                this::onMessage,
                (code, reason) -> log.info("Binance SDK WebSocket 正在关闭: code={}, reason={}", code, reason),
                (code, reason) -> log.info("Binance SDK WebSocket 已关闭: code={}, reason={}", code, reason),
                (throwable, response) -> {
                    String responseText = response == null ? "null" : response.toString();
                    log.error("Binance SDK WebSocket 连接失败: response={}, error={}", responseText, throwable.getMessage(), throwable);
                }
        );
        connectionIds.add(connectionId);

        log.info("Binance SDK WebSocket 已启动，connectionId={}, streams={}", connectionId, streams);
        return connectionId;
    }

    public void onMessage(String message) {
        if (!running.get()) {
            return;
        }
        log.info("收到 Binance 行情数据: {}", message);
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
        submitMarketTask("kline", source.getS(), () -> {
            marketMemoryCache.putKline(source.getS(), kline.getI(), kline);
            marketDataPublisher.publish("kline", source.getS(), kline.getI(), kline);
        });
    }

    private void cacheTicker(String message) throws JsonProcessingException {
        TickerWsMessageDo wsMsg = objectMapper.readValue(message, TickerWsMessageDo.class);
        TickerWsMessageDo.Source ticker = wsMsg.getData();
        submitMarketTask("ticker", ticker.getS(), () -> {
            marketMemoryCache.putTicker(ticker.getS(), ticker);
            marketDataPublisher.publish("ticker", ticker.getS(), null, ticker);
        });
    }

    private void cacheDepth(String message) throws JsonProcessingException {
        DepthWsMessageDo wsMsg = objectMapper.readValue(message, DepthWsMessageDo.class);
        DepthWsMessageDo.Source depth = wsMsg.getData();
        submitMarketTask("depth", depth.getS(), () -> {
            marketMemoryCache.putDepth(depth.getS(), depth);
            marketDataPublisher.publish("depth", depth.getS(), null, depth);
        });
    }

    private void cacheTrade(String message) throws JsonProcessingException {
        TradeWsMessageDo wsMsg = objectMapper.readValue(message, TradeWsMessageDo.class);
        TradeWsMessageDo.Source trade = wsMsg.getData();
        submitMarketTask("trade", trade.getS(), () -> {
            marketMemoryCache.putTrade(trade.getS(), trade);
            marketDataPublisher.publish("trade", trade.getS(), null, trade);
        });
    }

    private void submitMarketTask(String type, String symbol, Runnable task) {
        if (!running.get()) {
            return;
        }
        try {
            CompletableFuture.runAsync(task, marketExecutor).exceptionally(ex -> {
                log.error("处理Binance行情失败: type={}, symbol={}", type, symbol, ex);
                return null;
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
