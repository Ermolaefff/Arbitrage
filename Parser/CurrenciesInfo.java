package MONEYpackage.Parser;

import MONEYpackage.Algorithms.FindersSequence;
import MONEYpackage.Algorithms.StringST;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Parser.ParsingInfo.ParsingData;
import MONEYpackage.Parser.ParsingInfo.ParsingInfo;

import java.io.*;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CurrenciesInfo {
    private int size = 0;
    private final StringST<Integer>[] ST;
    private final StringST<Integer> exceptions = new StringST<>();
    private final String[] nameByIdx;
    private final StockMarkets[] marketByIdx;

    public CurrenciesInfo(ParsingInfo parsingInfo) throws IOException, InterruptedException {
        String exceptionsFilePath = "./src/MONEYpackage/ParseExceptions.txt";
        getExceptionsFromFile(exceptionsFilePath);

        final int numberOfThreads = StockMarkets.values().length;
        ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);
        ST = new StringST[StockMarkets.values().length];
        for (StockMarkets market : StockMarkets.values()) {
            ST[market.ordinal()] = new StringST<>();
            pool.execute( () -> {
                try {
                    parse(parsingInfo.get(market));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }


        pool.shutdown();
        if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
            throw new RuntimeException("Error (currencies info) :: server timeout exceeded ");
        }

        marketByIdx = new StockMarkets[size];
        nameByIdx = new String[size];
        for(StockMarkets market : StockMarkets.values()) {
            int marketIdx = market.ordinal();
            for (String currency : ST[marketIdx].keys()) {
                int currencyIdx = ST[marketIdx].get(currency);
                nameByIdx[currencyIdx] = currency;
                marketByIdx[currencyIdx] = market;
            }
        }
    }

    public boolean contains(Object currency, String stockMarketName) {
        return ST[StockMarkets.valueOf(stockMarketName).ordinal()].contains(currency);
    }

    public boolean contains(Object currency, StockMarkets stockMarket) {
        return ST[stockMarket.ordinal()].contains(currency);
    }

    public Iterable<String> keys(String stockMarketName) {
        return ST[StockMarkets.valueOf(stockMarketName).ordinal()].keys();
    }

    public Iterable<String> keys(StockMarkets market) {
        return ST[market.ordinal()].keys();
    }

    public String getCurrencyName(int idx) {
        return nameByIdx[idx];
    }

    public String getFullName(int idx) {
        return nameByIdx[idx] + " (" + getStockMarket(idx) + ")";
    }

    public StockMarkets getStockMarket(int idx) {
        return marketByIdx[idx];
    }

    public int getSize() {
        return size;
    }

    public int getIdx(String currency, String stockMarketName) {
        return ST[StockMarkets.valueOf(stockMarketName).ordinal()].get(currency);
    }

    public int getIdx(String currency, StockMarkets market) {
        return ST[market.ordinal()].get(currency);
    }

    private void getExceptionsFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        for (String currency = reader.readLine(); currency != null; currency = reader.readLine())
           exceptions.put(currency, exceptions.size());
    }

    private void parse(ParsingData market) throws IOException {

        InputStream is = new URL(market.exchangeInfoURL).openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        FindersSequence finders = market.currenciesFinders;
        int marketIdx = market.stockMarket.ordinal();

        for (int symbol = bufferedReader.read(); symbol != -1; symbol = bufferedReader.read()) {
            if (finders.isSubstringFound(symbol)) {
                String currencyName = "";
                for (int c = bufferedReader.read(); c != finders.getCurrentSeparator(); c = bufferedReader.read())
                    currencyName += (char) c;

                synchronized (ST) {
                    if (!exceptions.contains(currencyName))
                        ST[marketIdx].put(currencyName, size++);
                }

                finders.switchToTheNextFinder();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //CurrenciesInfo currenciesInfo = new CurrenciesInfo(new ParsingInfo());
    }
}
