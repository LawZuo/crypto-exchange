package coin.exchange.module.datasrouce.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeWsMessageDo {

    private String stream;
    private Source data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String e;

        @JsonProperty("E")
        private Long eventTime;

        private String s;
        private Long t;
        private BigDecimal p;
        private BigDecimal q;

        @JsonProperty("T")
        private Long tradeTime;

        private Boolean m;
    }
}
