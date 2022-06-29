package MONEYpackage.Parser.ParsingInfo.StockMarkets;

import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Algorithms.Finder;
import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Parser.NumberTransformers.DoubleTransformer;
import MONEYpackage.Parser.ParsingInfo.ParsingData;

public class KuCoin extends ParsingData {
    public void initialize() {
        stockMarket = StockMarkets.KuCoin;
        commission = 0.999;

        pricesTickerURL = "https://api.kucoin.com/api/v1/market/allTickers";
        exchangeInfoURL = "https://api.kucoin.com/api/v1/symbols";
        singlePriceURLBegin = "https://api.kucoin.com/api/v1/market/orderbook/level1?symbol=";
        singlePriceURLEnd = "";

        exchangeInfoFinders = new FindersSequence(7);
        exchangeInfoFinders.addFinder(ParsingFields.MarketName, "name\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.BaseAsset, "baseCurrency\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.QuoteAsset, "quoteCurrency\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.BaseMinSize, "baseMinSize\":\"", '\"', new DoubleTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.QuoteMinSize, "quoteMinSize\":\"", '\"', new DoubleTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.BasePrecision, "baseIncrement\":\"", '\"', new DoubleTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.QuotePrecision, "quoteIncrement\":\"", '\"', new DoubleTransformer());

        currenciesFinders = new FindersSequence(2);
        currenciesFinders.addFinder(ParsingFields.BaseAsset, "baseCurrency\":\"", '\"', null);
        currenciesFinders.addFinder(ParsingFields.QuoteAsset, "quoteCurrency\":\"", '\"', null);

        pricesFinders = new FindersSequence(3);
        pricesFinders.addFinder(ParsingFields.MarketName, "symbolName\":\"", '\"', null);
        pricesFinders.addFinder(ParsingFields.SellPrice, "buy\":\"", '\"', new DoubleTransformer());
        pricesFinders.addFinder(ParsingFields.BuyPrice, "sell\":\"", '\"', new DoubleTransformer());

        sellPriceFinder = new Finder("bestBid\":\"", '\"');
        buyPriceFinder = new Finder("bestAsk\":\"", '\"');


    }
}
