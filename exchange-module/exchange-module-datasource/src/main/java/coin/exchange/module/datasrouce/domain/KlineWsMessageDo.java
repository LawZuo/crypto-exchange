package coin.exchange.module.datasrouce.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlineWsMessageDo {

    private String stream; // stream流id
    private Source data; // 数据

    @JsonProperty("D")
    private KlineWsMessageDo.Source source;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {

        @JsonProperty("E")
        private Long eventTime;  // 事件时间
        private String s;       // 交易对
        private String e;       // 事件类型 "kline"

        @JsonProperty("k")
        private KlineWsMessageDo.Source.Kline kline;    // ✅ 嵌套的K线对象

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Kline {
            private Long t;     // 开始时间

            @JsonProperty("T")
            private Long closeTime;     // 结束时间

            private String s;   // 交易对
            private String i;   // 间隔

            private BigDecimal o; // ✅ 开盘价 - BigDecimal!
            private BigDecimal c; // ✅ 收盘价
            private BigDecimal h; // ✅ 最高价
            private BigDecimal l; // ✅ 最低价
            private BigDecimal v; // ✅ 成交量

            private Long n;       // 成交笔数
            private Boolean x;    // ✅ 是否已闭合
            private BigDecimal q; // 成交额

            @JsonProperty("V")
            private BigDecimal takerBuyVolume; // 主动买入量

            @JsonProperty("Q")
            private BigDecimal takerBuyTurnover; // 主动买入额
        }
    }


    // {
    //     "e": "kline",           // 事件类型
    //         "E": 1672515782136,     // 事件时间 (ms)
    //         "s": "BTCUSDT",         // 交易对
    //         "k": {                  // ✅ K线数据核心对象
    //             "t": 1672515780000,   // K线开始时间
    //             "T": 1672515839999,   // K线结束时间
    //             "s": "BTCUSDT",       // 交易对
    //             "i": "1m",            // K线间隔
    //             "f": 100,             // 第一笔成交ID
    //             "L": 200,             // 最后一笔成交ID
    //             "o": "0.0010",        // 开盘价 (String!)
    //             "c": "0.0020",        // 收盘价 (String!)
    //             "h": "0.0025",        // 最高价 (String!)
    //             "l": "0.0015",        // 最低价 (String!)
    //             "v": "1000",          // 成交量 (String!)
    //             "n": 100,             // 成交笔数
    //             "x": false,           // ✅ 是否已收盘 (false=未闭合, true=已闭合)
    //             "q": "1.0000",        // 成交额
    //             "V": "200",           // 主动买入成交量
    //             "Q": "0.2000",        // 主动买入成交额
    //             "B": "123456"         // 忽略
    // }
    // }
}
