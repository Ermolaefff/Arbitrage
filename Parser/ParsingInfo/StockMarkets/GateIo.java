package MONEYpackage.Parser.ParsingInfo.StockMarkets;

import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Algorithms.Finder;
import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Parser.NumberTransformers.DoubleTransformer;
import MONEYpackage.Parser.NumberTransformers.IntegerTransformer;
import MONEYpackage.Parser.ParsingInfo.ParsingData;

public class GateIo extends ParsingData {
    public void initialize() {
        stockMarket = StockMarkets.GateIo;
        commission = 0.998;

        pricesTickerURL = "https://api.gateio.ws/api/v4/spot/tickers";
        exchangeInfoURL = "https://api.gateio.ws/api/v4/spot/currency_pairs";
        singlePriceURLBegin = "https://api.gateio.ws/api/v4/spot/tickers?currency_pair=";
        singlePriceURLEnd = "";

        exchangeInfoFinders = new FindersSequence(5);
        exchangeInfoFinders.addFinder(ParsingFields.MarketName, "id\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.BaseAsset, "base\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.QuoteAsset, "quote\":\"", '\"', null);
        exchangeInfoFinders.addFinder(ParsingFields.MinLotSize, "min_quote_amount\":\"", '\"', new DoubleTransformer());
        exchangeInfoFinders.addFinder(ParsingFields.BasePrecision, "amount_precision\":", ',', new IntegerTransformer());

        currenciesFinders = new FindersSequence(2);
        currenciesFinders.addFinder(ParsingFields.BaseAsset, "base\":\"", '\"', null);
        currenciesFinders.addFinder(ParsingFields.QuoteAsset, "quote\":\"", '\"', null);

        pricesFinders = new FindersSequence(3);
        pricesFinders.addFinder(ParsingFields.MarketName, "currency_pair\":\"", '\"', null);
        pricesFinders.addFinder(ParsingFields.BuyPrice, "lowest_ask\":\"", '\"', new DoubleTransformer());
        pricesFinders.addFinder(ParsingFields.SellPrice, "highest_bid\":\"", '\"', new DoubleTransformer());

        buyPriceFinder = new Finder("lowest_ask\":\"", '\"');
        sellPriceFinder = new Finder("highest_bid\":\"", '\"');
    }
}
