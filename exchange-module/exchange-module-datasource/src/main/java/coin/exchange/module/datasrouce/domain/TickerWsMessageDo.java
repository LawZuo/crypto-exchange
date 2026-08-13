package coin.exchange.module.datasrouce.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerWsMessageDo {

    private String stream;
    private Source data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String e;

        @JsonProperty("E")
        private Long eventTime;

        private String s;
        private BigDecimal p;

        @JsonProperty("P")
        private BigDecimal priceChangePercent;

        private BigDecimal w;
        private BigDecimal x;
        private BigDecimal c;

        @JsonProperty("Q")
        private BigDecimal lastQuantity;

        private BigDecimal b;

        @JsonProperty("B")
        private BigDecimal bestBidQuantity;

        private BigDecimal a;

        @JsonProperty("A")
        private BigDecimal bestAskQuantity;

        private BigDecimal o;
        private BigDecimal h;
        private BigDecimal l;
        private BigDecimal v;
        private BigDecimal q;

        @JsonProperty("O")
        private Long openTime;

        @JsonProperty("C")
        private Long closeTime;

        @JsonProperty("F")
        private Long firstTradeId;

        @JsonProperty("L")
        private Long lastTradeId;

        private Long n;
    }
}
