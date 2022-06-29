package MONEYpackage.Graph;

import MONEYpackage.Enums.TradingMode;
import MONEYpackage.Enums.StockMarkets;

public class DirectedEdge {
    private final int from;
    private final int to;
    private final double weight;
    private final double commission;
    private StockMarkets stockMarket = null;
    private final String marketName;
    private final TradingMode tradingMode;
    private boolean stopped = false;

    public DirectedEdge(int from, int to, double weight, double commission, StockMarkets stockMarket, String marketName, TradingMode tradingMode) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.commission = commission;
        this.stockMarket = stockMarket;
        this.marketName = marketName;
        this.tradingMode = tradingMode;
    }


    public DirectedEdge(int from, int to, double percentCommission, double staticCommission, String networkName, boolean isStopped) {
        this.from = from;
        this.to = to;
        this.weight = percentCommission;
        this.commission = staticCommission;
        this.marketName = networkName;
        this.tradingMode = TradingMode.Transfer;
        this.stopped = isStopped;
    }

    public boolean isStopped() {
        return stopped;
    }

    public TradingMode sellMode() {
        return tradingMode;
    }

    public String marketName() {
        return marketName;
    }

    public double commission() {
        return commission;
    };

    public StockMarkets stockMarket() {
        return stockMarket;
    }

    public double weight() {
        return weight;
    }

    public int to() {
        return to;
    }

    public int from() {
        return from;
    }

    public String toString() {
        return String.format("%d->%d %.2f", from, to, weight);
    }

    public static void main(String[] args) {
    }
}
