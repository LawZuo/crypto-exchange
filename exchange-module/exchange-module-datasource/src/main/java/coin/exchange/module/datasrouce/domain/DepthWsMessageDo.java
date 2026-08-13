package coin.exchange.module.datasrouce.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepthWsMessageDo {

    private String stream;
    private Source data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String e;

        @JsonProperty("E")
        private Long eventTime;

        private String s;

        @JsonProperty("U")
        private Long firstUpdateId;

        private Long u;

        @JsonProperty("b")
        private List<List<BigDecimal>> bids;

        @JsonProperty("a")
        private List<List<BigDecimal>> asks;
    }
}
