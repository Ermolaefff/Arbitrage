package MONEYpackage.Parser.ParsingInfo.StockMarkets;

import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Algorithms.Finder;
import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Parser.NumberTransformers.DoubleTransformer;
import MONEYpackage.Parser.NumberTransformers.IntegerTransformer;
import MONEYpackage.Parser.ParsingInfo.ParsingData;

public class Binance extends ParsingData {
    public void initialize() {
        stockMarket = StockMarkets.Binance;
        commission = 0.999;

        pricesTickerURL = "https://api1.binance.com/api/v3/ticker/24hr";
        exchangeInfoURL = "https://api1.binance.com/api/v3/exchangeInfo";
        singlePriceURLBegin = "https://api.binance.com/api/v1/depth?symbol=";
        singlePriceURLEnd = "&limit=1";

        exchangeInfoFinders = new FindersSequence(6);
        exchangeInfoFinders.addFinder(ParsingFields.MarketName, "symbol\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.BaseAsset, "baseAsset\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.BasePrecision, "baseAssetPrecision\":", ',', new IntegerTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.QuoteAsset, "quoteAsset\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.QuotePrecision, "quoteAssetPrecision\":", ',', new IntegerTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.MinLotSize, "LOT_SIZE\",\"minQty\":\"", '\"', new DoubleTransformer());

        currenciesFinders = new FindersSequence(2);
        currenciesFinders.addFinder(ParsingFields.BaseAsset, "baseAsset\":\"", '\"', null);
        currenciesFinders.addFinder(ParsingFields.QuoteAsset, "quoteAsset\":\"", '\"', null);

        pricesFinders = new FindersSequence(3);
        pricesFinders.addFinder(ParsingFields.MarketName, "symbol\":\"", '\"', null);
        pricesFinders.addFinder(ParsingFields.SellPrice, "bidPrice\":\"", '\"', new DoubleTransformer());
        pricesFinders.addFinder(ParsingFields.BuyPrice, "askPrice\":\"", '\"', new DoubleTransformer());

        sellPriceFinder = new Finder("bids\":[[\"", '\"');
        buyPriceFinder = new Finder("asks\":[[\"", '\"');

    }
}

