package MONEYpackage.Parser.ParsingInfo;

import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Parser.ParsingInfo.StockMarkets.Binance;
import MONEYpackage.Parser.ParsingInfo.StockMarkets.FTX;
import MONEYpackage.Parser.ParsingInfo.StockMarkets.GateIo;
import MONEYpackage.Parser.ParsingInfo.StockMarkets.KuCoin;

public class ParsingInfo {
    private final ParsingData[] markets;

    public ParsingInfo() {
        markets = new ParsingData[StockMarkets.values().length];
        markets[StockMarkets.Binance.ordinal()] = new Binance();
        markets[StockMarkets.FTX.ordinal()] = new FTX();
        markets[StockMarkets.KuCoin.ordinal()] = new KuCoin();
        markets[StockMarkets.GateIo.ordinal()] = new GateIo();

        for(ParsingData market : markets)
            market.initialize();
    }

    public ParsingData get(StockMarkets market) {
        return markets[market.ordinal()];
    }
}
