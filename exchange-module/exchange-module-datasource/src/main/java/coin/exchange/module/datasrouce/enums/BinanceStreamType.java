package coin.exchange.module.datasrouce.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BinanceStreamType {

    TICKER("ticker"),
    TRADE("trade"),
    KLINE("kline"),
    DEPTH("depth");

    private final String streamName;
}
