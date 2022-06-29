package MONEYpackage.Parser;

import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Enums.TradingMode;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Graph.DirectedEdge;
import MONEYpackage.Algorithms.Finder;
import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Graph.WeightedDigraph;
import MONEYpackage.Parser.ParsingInfo.ParsingData;
import MONEYpackage.Parser.ParsingInfo.ParsingInfo;
import MONEYpackage.Parser.TransferInfo.TransferInfo;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PricesInfo {

    TransferInfo transferInfo;
    CurrenciesInfo currenciesInfo;
    ExchangeInfo exchangeInfo;
    ParsingInfo parsingInfo;
    WeightedDigraph graph;

    public PricesInfo(CurrenciesInfo currenciesInfo, ExchangeInfo exchangeInfo, ParsingInfo parsingInfo) throws IOException, ParseException {
        this.currenciesInfo = currenciesInfo;
        this.exchangeInfo = exchangeInfo;
        this.parsingInfo = parsingInfo;
        transferInfo = new TransferInfo(currenciesInfo);
    }

    public WeightedDigraph getAllPrices() throws InterruptedException {
        graph = new WeightedDigraph(currenciesInfo.getSize());

        final int numberOfThreads = StockMarkets.values().length;
        ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);
        for (StockMarkets market : StockMarkets.values())
            pool.execute(() -> {
                try {
                    parse(parsingInfo.get(market));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        pool.shutdown();
        pool.awaitTermination(20, TimeUnit.SECONDS);

        for (DirectedEdge edge : transferInfo.transferEdges())
            graph.addEdge(edge);

        return graph;
    }

    public double getSinglePrice(ParsingData market, String marketName, TradingMode sellMode) throws IOException {
        InputStream is = new URL(market.singlePriceURLBegin + marketName + market.singlePriceURLEnd).openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        Finder finder = (sellMode == TradingMode.Sell) ? market.sellPriceFinder : market.buyPriceFinder;
        char separator = finder.getSeparator();

        for (int symbol = bufferedReader.read(); symbol != -1; symbol = bufferedReader.read()) {
            if (finder.isSubstringFound(symbol)) {
                String strPrice = "";
                for (int c = bufferedReader.read(); c != separator; c = bufferedReader.read())
                    strPrice += (char) c;

                double price = Double.parseDouble(strPrice);

                return sellMode == TradingMode.Sell ? price :  1 / price;
            }
        }
        return -1;
    }


    private void parse(ParsingData market) throws IOException {
        InputStream is = new URL(market.pricesTickerURL).openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        FindersSequence finders = market.pricesFinders;

        for (int symbol = bufferedReader.read(); symbol != -1; symbol = bufferedReader.read()) {
            if (finders.isSubstringFound(symbol)) {
                if (finders.isFinish()) {
                    addInfoToGraph(finders, market);
                    finders.refreshFields();
                }

                String result = "";
                char separator = finders.getCurrentSeparator();
                for (int c = bufferedReader.read(); c != separator; c = bufferedReader.read())
                    result += (char) c;

                finders.addField(result);
                finders.switchToTheNextFinder();
            }
        }
        addInfoToGraph(finders, market);
    }

    private void addInfoToGraph(FindersSequence finders, ParsingData market) {
        String marketName = (String) finders.getField(ParsingFields.MarketName);
        double sellPrice = (Double) finders.getField(ParsingFields.SellPrice);
        double buyPrice = (Double) finders.getField(ParsingFields.BuyPrice);

        StockMarkets stockMarket = market.stockMarket;

        if (exchangeInfo.contains(stockMarket, marketName) && sellPrice != 0 && buyPrice != 0) {
            String baseAsset = (String) exchangeInfo.get(stockMarket, marketName, ParsingFields.BaseAsset);
            String quoteAsset = (String) exchangeInfo.get(stockMarket, marketName, ParsingFields.QuoteAsset);

            int baseIdx = currenciesInfo.getIdx(baseAsset, stockMarket);
            int quoteIdx = currenciesInfo.getIdx(quoteAsset, stockMarket);

            synchronized (graph) {
                graph.addEdge(new DirectedEdge(baseIdx, quoteIdx, sellPrice, market.commission,
                        stockMarket, marketName, TradingMode.Sell));
                graph.addEdge(new DirectedEdge(quoteIdx, baseIdx, 1 / buyPrice, market.commission,
                        stockMarket, marketName, TradingMode.Buy));
            }
        }
    }
}
