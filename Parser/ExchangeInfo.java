package MONEYpackage.Parser;

import MONEYpackage.Algorithms.StringST;
import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Parser.ParsingInfo.ParsingData;
import MONEYpackage.Parser.ParsingInfo.ParsingInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExchangeInfo {
    private final StringST<Object[]>[] ST = new StringST[StockMarkets.values().length];
    private final CurrenciesInfo currenciesInfo;

    public ExchangeInfo(ParsingInfo parsingInfo, CurrenciesInfo currenciesInfo) throws InterruptedException {
        this.currenciesInfo = currenciesInfo;
        for (int i = 0; i < ST.length; i++)
            ST[i] = new StringST<>();


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
        if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
            throw new RuntimeException("Error (exchange info) :: server timeout exceeded");
        }
    }

    public Object get(StockMarkets stockMarket, String marketName, ParsingFields field) {
        return ST[stockMarket.ordinal()].get(marketName)[field.ordinal()];
    }

    public boolean contains(StockMarkets stockMarket, String marketName) {
        return ST[stockMarket.ordinal()].get(marketName) != null;
    }

    private void parse(ParsingData market) throws IOException {
        InputStream is = new URL(market.exchangeInfoURL).openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        FindersSequence finders = market.exchangeInfoFinders;
        StockMarkets stockMarket = market.stockMarket;
        String baseAsset, quoteAsset, marketName;

        for (int symbol = bufferedReader.read(); symbol != -1; symbol = bufferedReader.read()) {
            if (finders.isSubstringFound(symbol)) {

                if (finders.isFinish()) {
                    baseAsset = (String) finders.getField(ParsingFields.BaseAsset);
                    quoteAsset = (String) finders.getField(ParsingFields.QuoteAsset);

                    if (currenciesInfo.contains(baseAsset, stockMarket) && currenciesInfo.contains(quoteAsset, stockMarket)) {
                        marketName = (String) finders.getField(ParsingFields.MarketName);
                        ST[stockMarket.ordinal()].put(marketName, finders.getAllFields());
                    }

                    finders.refreshFields();
                }

                String result = "";
                for (int c = bufferedReader.read(); c != finders.getCurrentSeparator(); c = bufferedReader.read())
                    result += (char) c;

                finders.addField(result);
                finders.switchToTheNextFinder();
            }
        }
        marketName = (String) finders.getField(ParsingFields.MarketName);
        ST[stockMarket.ordinal()].put(marketName, finders.getAllFields());
    }

    public static void main(String[] args) throws IOException {
    }
}



