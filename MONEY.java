package MONEYpackage;

import java.text.SimpleDateFormat;
import java.util.Date;

import MONEYpackage.Enums.TradingMode;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Graph.Arbitrage;
import MONEYpackage.Graph.DirectedEdge;
import MONEYpackage.Graph.WeightedDigraph;
import MONEYpackage.Parser.CurrenciesInfo;
import MONEYpackage.Parser.ExchangeInfo;
import MONEYpackage.Parser.ParsingInfo.ParsingInfo;
import MONEYpackage.Parser.PricesInfo;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Queue;

public class MONEY {
    private ParsingInfo parsingInfo;
    private ExchangeInfo exchangeInfo;
    private CurrenciesInfo currenciesInfo;
    private PricesInfo pricesInfo;
    private double startStake;
    private int startCurrencyIdx;
    private String cycle;
    private String operations;
    private Iterable<DirectedEdge> maxCycle;
    private boolean wasCycleFound = false;
    private double delayDuration = 0;
    private double profit = 0;
    private double minProfitAsPercent = 0;
    public boolean wasCycleRelevant = false;
    public double avrProfit = 0;

    public MONEY(double startStake, String startCurrency, StockMarkets startMarket, double minProfitAsPercent) throws IOException, InterruptedException, ParseException {
        try {
            parsingInfo = new ParsingInfo();
            System.out.println("Making a list of cryptocurrencies...");
            currenciesInfo = new CurrenciesInfo(parsingInfo);
            System.out.println("Uploading exchange data...");
            exchangeInfo = new ExchangeInfo(parsingInfo, currenciesInfo);
            pricesInfo = new PricesInfo(currenciesInfo, exchangeInfo, parsingInfo);

            this.startStake = startStake;
            this.startCurrencyIdx = currenciesInfo.getIdx(startCurrency, startMarket);
            this.minProfitAsPercent = minProfitAsPercent;

        } catch (RuntimeException exception) {
            exception.printStackTrace();
        }
    }

    public String getCycleAsString() throws IOException, InterruptedException {
        resetTheState();
        if (!wasCycleFound) {
            WeightedDigraph G = pricesInfo.getAllPrices();
            findMaxCycle(G);
           // prepareCycle();
        }

        long begTime = System.currentTimeMillis();
        if (maxCycle != null)
            checkCycle();
        long endTime = System.currentTimeMillis();

        delayDuration = (endTime - begTime) / 1000.0;
        wasCycleFound = getProfitAsPercent() > minProfitAsPercent;

        return wasCycleFound ? cycle : "";
    }

    public void checkCycleDuration() throws IOException, InterruptedException {
        resetTheState();
        WeightedDigraph G = pricesInfo.getAllPrices();
        findMaxCycle(G);
        prepareCycle(G);

        if (maxCycle != null) {
            double accumulator = 0;
            int cnt = 0;

            long begTime = System.currentTimeMillis();


            for (wasCycleRelevant = (getProfitAsPercent() >= minProfitAsPercent);
                 wasCycleRelevant && (++cnt) % 100 != 0;
                 wasCycleRelevant = (getProfitAsPercent() >= 0)) {
                accumulator += getProfitAsPercent();
                checkCycle();
                System.out.println("Profit:: " + getProfitAsPercent());
            }
            long endTime = System.currentTimeMillis();

            delayDuration = (endTime - begTime) / 1000.0;
            avrProfit = accumulator / (double) cnt;

            if (cnt > 0) {
                printCycleInfo();
                logCycle();
            }

        }
    }

    public void logCycle() {
        String out = "AVERAGE PROFIT:: " + avrProfit + " %\nTOTAL DURATION:: " + delayDuration + "\n";
        try {
            Files.write(Paths.get("./src/MONEYpackage/Logs.txt"), out.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void printCycleInfo() {
        System.out.println("Cycle:\n" + cycle);
        System.out.println(operations);
        System.out.printf("RESULT:: %.10f -> %.10f\n", startStake, profit + startStake);
        System.out.printf("DELAY:: %.5f sec\n", delayDuration);
        System.out.printf("PROFIT:: %.5f %%\n", getProfitAsPercent());
        System.out.println("-----------------------------------------------");
    }

    public void writeCycleInfoToFile() {
        if (getProfitAsPercent() < 3) return;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());

        String out = formatter.format(date) + "\n";
        out += "PROFIT:: " + getProfitAsPercent() + " %\nDELAY:: " + delayDuration + "\n" + operations;

        try {
            Files.write(Paths.get("./src/MONEYpackage/Logs.txt"), out.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(e);
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        MONEY moneyMaker = new MONEY(1000, "USDT", StockMarkets.Binance, 1);

        int i = 0;
        while (true) {
            try {
                System.out.println("Attempt:: " + ++i);
                moneyMaker.checkCycleDuration();
            }
            catch (java.lang.Throwable e) {
                e.printStackTrace();
                if (moneyMaker.wasCycleRelevant)
                    moneyMaker.logCycle();
                Thread.sleep(60*1000);
            }
/*
            long t1 = System.currentTimeMillis();
            String cycle = moneyMaker.getCycleAsString();
            long t2 = System.currentTimeMillis();
            System.out.println("TOTAL TIME:: " + (t2 -t1) / 1000.0);
            if (cycle.equals("")) {
                System.out.println("Cycle failed");
                continue;
            }
            moneyMaker.printCycleInfo();
            moneyMaker.writeCycleInfoToFile();

 */


        }

    }

    private void checkCycle() throws IOException {
        double currentStake = startStake;
        cycle = "";
        operations = "";

        for (DirectedEdge e : maxCycle) {
            int from = e.from();
            int to = e.to();
            double commission = e.commission();
            TradingMode sellMode = e.sellMode();
            String startAsset = currenciesInfo.getFullName(from);
            String endAsset = currenciesInfo.getFullName(to);
            String marketName = e.marketName();

            // double roundVal = (Double) exchangeInfo.get(e.stockMarket(), marketName, ParsingFields.BasePrecision);
            // if (sellMode == SellMode.SELL) currentStake = round(currentStake, roundVal);

            operations +=  currentStake + " " + startAsset + " -> ";

            if (e.sellMode() != TradingMode.Transfer) {
                cycle += sellMode + " " + marketName + " " + e.stockMarket() + "\n"; //round value was here

                double price = pricesInfo.getSinglePrice(parsingInfo.get(e.stockMarket()), marketName, sellMode);
                currentStake *= price;
                //   if (sellMode == SellMode.BUY) currentStake = round(currentStake, roundVal);
                currentStake *= commission;

                operations +=  currentStake + " " + endAsset + " PRICE:: " + price + "\n";
            }
            else {
                cycle += sellMode + " " + currenciesInfo.getCurrencyName(from) + " " + marketName + " ";
                cycle += currenciesInfo.getStockMarket(from) + " " + currenciesInfo.getStockMarket(to) + "\n";
                currentStake = currentStake * e.weight() - commission;
                operations +=  currentStake + " " + endAsset + " COMMISSION:: " + commission + "\n";
            }
        }
        profit = currentStake - startStake;
    }

    private int findStartIdxInCycle() {
        int startIdx = -1;
        for (DirectedEdge e : maxCycle) {
            startIdx++;
            if (e.from() == startCurrencyIdx)
                return startIdx;
        }
        return -1;
    }

    private void prepareCycle(WeightedDigraph G) {
        if (maxCycle == null) return;

        int startIdx = findStartIdxInCycle();
        if (startIdx == 0) return;

        Queue<DirectedEdge> queue1 = new LinkedList<>();
        Queue<DirectedEdge> queue2 = new LinkedList<>();

        if (startIdx == -1) {
           /*   int i = 0;
            for (DirectedEdge e : maxCycle) {
                double weight = G.weight(startIdx, e.from());

                if (weight != -1) {/////////////////////
                    startIdx = i;
                    startStake *=
                }



                i++;
            }
            */

            maxCycle = null;
            return;
        }

        int i = 0;
        for (DirectedEdge e : maxCycle) {
            if (i++ < startIdx) queue2.add(e);
            else                queue1.add(e);
        }

        queue1.addAll(queue2);
        maxCycle = queue1;
    }

    public double getDelayDuration() {
        return delayDuration;
    }

    public double getProfitAsPercent() {
        return profit * 100 / startStake;
    }

    public double getStartStake() {
        return startStake;
    }

    public double getProfit() {
        return profit;
    }

    public String getOperations() {
        return operations;
    }

    private void resetTheState() {
        cycle = "";
        operations = "";
        delayDuration = 0;
        profit = 0;
        if (!wasCycleFound) maxCycle = null;
    }

    private void findMaxCycle(WeightedDigraph G) {
        maxCycle = null;
        double commissionDelta = 0.0005, delta = 0;

        Arbitrage arbitrage = new Arbitrage(G, startCurrencyIdx, delta);
        while (arbitrage.hasCycle()) {
            maxCycle = arbitrage.getCycle();
            delta += commissionDelta;
            arbitrage = new Arbitrage(G, startCurrencyIdx, delta);
        }
    }
}


