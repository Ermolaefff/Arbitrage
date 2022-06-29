package MONEYpackage.Parser.ParsingInfo.StockMarkets;

import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Algorithms.Finder;
import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Parser.NumberTransformers.DoubleTransformer;
import MONEYpackage.Parser.ParsingInfo.ParsingData;

public class FTX extends ParsingData {
    public void initialize() {
        stockMarket = StockMarkets.FTX;
        commission = 0.9993;

        pricesTickerURL = "https://ftx.com/api/markets";
        exchangeInfoURL = pricesTickerURL;
        singlePriceURLBegin = pricesTickerURL + "/";
        singlePriceURLEnd = "";

        exchangeInfoFinders = new FindersSequence(4);
        exchangeInfoFinders.addFinder(ParsingFields.MarketName, "name\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.MinLotSize, "sizeIncrement\":", ',', new DoubleTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.BaseAsset, "baseCurrency\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.QuoteAsset, "quoteCurrency\":\"", '\"', null);

        currenciesFinders = new FindersSequence(2);
        currenciesFinders.addFinder(ParsingFields.BaseAsset, "baseCurrency\":\"", '\"', null);
        currenciesFinders.addFinder(ParsingFields.QuoteAsset, "quoteCurrency\":\"", '\"', null);

        pricesFinders = new FindersSequence(3);
        pricesFinders.addFinder(ParsingFields.MarketName, "name\":\"", '\"', null);
        pricesFinders.addFinder(ParsingFields.SellPrice, "bid\":", ',', new DoubleTransformer());
        pricesFinders.addFinder(ParsingFields.BuyPrice, "ask\":", ',', new DoubleTransformer());

        sellPriceFinder = new Finder("bid\":", ',');
        buyPriceFinder = new Finder("ask\":", ',');
    }
}
