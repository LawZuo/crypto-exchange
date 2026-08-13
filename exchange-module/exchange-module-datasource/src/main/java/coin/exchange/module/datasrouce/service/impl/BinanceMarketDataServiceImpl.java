package coin.exchange.module.datasrouce.service.impl;

import coin.exchange.api.market.model.BinanceDepthVo;
import coin.exchange.api.market.model.BinanceKlineVo;
import coin.exchange.api.market.model.BinanceTickerVo;
import coin.exchange.api.market.model.BinanceTradeVo;
import coin.exchange.module.datasrouce.config.BinanceProperties;
import coin.exchange.module.datasrouce.service.BinanceMarketDataService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class BinanceMarketDataServiceImpl implements BinanceMarketDataService {

    private static final int DEFAULT_DEPTH_LIMIT = 100;
    private static final int DEFAULT_TRADE_LIMIT = 100;
    private static final int DEFAULT_KLINE_LIMIT = 500;
    private static final int MAX_LIMIT = 1000;

    private final BinanceProperties binanceProperties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public BinanceTickerVo getTicker(String symbol) {
        JsonNode root = get("/api/v3/ticker/24hr", uriBuilder -> uriBuilder
                .queryParam("symbol", normalizeSymbol(symbol))
                .build());
        BinanceTickerVo ticker = new BinanceTickerVo();
        ticker.setSymbol(root.path("symbol").asText());
        ticker.setPriceChange(decimal(root, "priceChange"));
        ticker.setPriceChangePercent(decimal(root, "priceChangePercent"));
        ticker.setWeightedAvgPrice(decimal(root, "weightedAvgPrice"));
        ticker.setPrevClosePrice(decimal(root, "prevClosePrice"));
        ticker.setLastPrice(decimal(root, "lastPrice"));
        ticker.setLastQuantity(decimal(root, "lastQty"));
        ticker.setBidPrice(decimal(root, "bidPrice"));
        ticker.setBidQuantity(decimal(root, "bidQty"));
        ticker.setAskPrice(decimal(root, "askPrice"));
        ticker.setAskQuantity(decimal(root, "askQty"));
        ticker.setOpenPrice(decimal(root, "openPrice"));
        ticker.setHighPrice(decimal(root, "highPrice"));
        ticker.setLowPrice(decimal(root, "lowPrice"));
        ticker.setVolume(decimal(root, "volume"));
        ticker.setQuoteVolume(decimal(root, "quoteVolume"));
        ticker.setOpenTime(root.path("openTime").asLong());
        ticker.setCloseTime(root.path("closeTime").asLong());
        ticker.setFirstTradeId(root.path("firstId").asLong());
        ticker.setLastTradeId(root.path("lastId").asLong());
        ticker.setTradeCount(root.path("count").asLong());
        return ticker;
    }

    @Override
    public BinanceDepthVo getDepth(String symbol, Integer limit) {
        JsonNode root = get("/api/v3/depth", uriBuilder -> uriBuilder
                .queryParam("symbol", normalizeSymbol(symbol))
                .queryParam("limit", normalizeLimit(limit, DEFAULT_DEPTH_LIMIT))
                .build());
        BinanceDepthVo depth = new BinanceDepthVo();
        depth.setLastUpdateId(root.path("lastUpdateId").asLong());
        depth.setBids(parsePriceLevels(root.path("bids")));
        depth.setAsks(parsePriceLevels(root.path("asks")));
        return depth;
    }

    @Override
    public List<BinanceTradeVo> listTrades(String symbol, Integer limit) {
        JsonNode root = get("/api/v3/trades", uriBuilder -> uriBuilder
                .queryParam("symbol", normalizeSymbol(symbol))
                .queryParam("limit", normalizeLimit(limit, DEFAULT_TRADE_LIMIT))
                .build());
        List<BinanceTradeVo> trades = new ArrayList<>();
        root.forEach(node -> {
            BinanceTradeVo trade = new BinanceTradeVo();
            trade.setId(node.path("id").asLong());
            trade.setPrice(decimal(node, "price"));
            trade.setQuantity(decimal(node, "qty"));
            trade.setQuoteQuantity(decimal(node, "quoteQty"));
            trade.setTime(node.path("time").asLong());
            trade.setBuyerMaker(node.path("isBuyerMaker").asBoolean());
            trade.setBestMatch(node.path("isBestMatch").asBoolean());
            trades.add(trade);
        });
        return trades;
    }

    @Override
    public List<BinanceKlineVo> listKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        JsonNode root = get("/api/v3/klines", uriBuilder -> {
            UriBuilder builder = uriBuilder
                    .queryParam("symbol", normalizeSymbol(symbol))
                    .queryParam("interval", normalizeInterval(interval))
                    .queryParam("limit", normalizeLimit(limit, DEFAULT_KLINE_LIMIT));
            if (startTime != null) {
                builder.queryParam("startTime", startTime);
            }
            if (endTime != null) {
                builder.queryParam("endTime", endTime);
            }
            return builder.build();
        });
        List<BinanceKlineVo> klines = new ArrayList<>();
        root.forEach(node -> {
            BinanceKlineVo kline = new BinanceKlineVo();
            kline.setOpenTime(node.path(0).asLong());
            kline.setOpenPrice(decimal(node, 1));
            kline.setHighPrice(decimal(node, 2));
            kline.setLowPrice(decimal(node, 3));
            kline.setClosePrice(decimal(node, 4));
            kline.setVolume(decimal(node, 5));
            kline.setCloseTime(node.path(6).asLong());
            kline.setQuoteAssetVolume(decimal(node, 7));
            kline.setTradeCount(node.path(8).asLong());
            kline.setTakerBuyBaseAssetVolume(decimal(node, 9));
            kline.setTakerBuyQuoteAssetVolume(decimal(node, 10));
            klines.add(kline);
        });
        return klines;
    }

    private JsonNode get(String path, Function<UriBuilder, URI> uriFunction) {
        return restClientBuilder
                .baseUrl(binanceProperties.getRestBaseUrl())
                .build()
                .get()
                .uri(uriBuilder -> uriFunction.apply(uriBuilder.path(path)))
                .retrieve()
                .body(JsonNode.class);
    }

    private List<List<BigDecimal>> parsePriceLevels(JsonNode levels) {
        List<List<BigDecimal>> result = new ArrayList<>();
        levels.forEach(level -> result.add(List.of(decimal(level, 0), decimal(level, 1))));
        return result;
    }

    private String normalizeSymbol(String symbol) {
        if (Objects.isNull(symbol) || symbol.isBlank()) {
            throw new IllegalArgumentException("交易对不能为空");
        }
        return symbol.trim().toUpperCase();
    }

    private String normalizeInterval(String interval) {
        if (Objects.isNull(interval) || interval.isBlank()) {
            throw new IllegalArgumentException("K线周期不能为空");
        }
        return interval.trim();
    }

    private int normalizeLimit(Integer limit, int defaultLimit) {
        if (limit == null || limit <= 0) {
            return defaultLimit;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        return new BigDecimal(node.path(fieldName).asText("0"));
    }

    private BigDecimal decimal(JsonNode node, int index) {
        return new BigDecimal(node.path(index).asText("0"));
    }
}
