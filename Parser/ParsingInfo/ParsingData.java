package MONEYpackage.Parser.ParsingInfo;

import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Algorithms.Finder;
import MONEYpackage.Algorithms.FindersSequence;

public class ParsingData {
    public StockMarkets stockMarket;
    public double commission;

    public String pricesTickerURL = "";
    public String exchangeInfoURL = "";
    public String singlePriceURLBegin = "";
    public String singlePriceURLEnd = "";

    public FindersSequence exchangeInfoFinders = null;
    public FindersSequence pricesFinders = null;
    public FindersSequence currenciesFinders = null;
    public Finder buyPriceFinder = null;
    public Finder sellPriceFinder = null;


    public void initialize() {};
}
