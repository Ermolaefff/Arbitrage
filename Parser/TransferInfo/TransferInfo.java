package MONEYpackage.Parser.TransferInfo;


import MONEYpackage.Algorithms.StringST;
import MONEYpackage.Enums.StockMarkets;
import MONEYpackage.Graph.DirectedEdge;
import MONEYpackage.Parser.CurrenciesInfo;
import MONEYpackage.Parser.NetworksInfo;
import MONEYpackage.Parser.ParsingInfo.ParsingInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class TransferInfo {
    private final Queue<DirectedEdge> transferEdges = new LinkedList<>();
    private final StringST<Queue<Integer>>[] deposit;
    private final StringST<Queue<Integer>>[] withdrawal;

    private final CurrenciesInfo currenciesInfo;
    private final NetworksInfo networksInfo;

    public TransferInfo(CurrenciesInfo currenciesInfo) throws IOException, ParseException {
        this.currenciesInfo = currenciesInfo;
        this.networksInfo = new NetworksInfo();

        int size = networksInfo.numberOfNetworks();

        deposit = new StringST[size];
        withdrawal = new StringST[size];

        for (int i = 0; i < size; i++){
            deposit[i] = new StringST<>();
            withdrawal[i] = new StringST<>();
        }

        for (StockMarkets stockMarket : StockMarkets.values())
           extractNetworksInfo(stockMarket);

        createTransferList();
    }

    private void extractNetworksInfo(StockMarkets stockMarket) {
        JSONObject jsonMarket = networksInfo.getMarketInfo(stockMarket);
        if (jsonMarket == null) return;


        for (String currencyName : currenciesInfo.keys(stockMarket)) {
            JSONObject jsonCurrency = (JSONObject) jsonMarket.get(currencyName);
            if (jsonCurrency == null) continue;

            int currencyIdx = currenciesInfo.getIdx(currencyName, stockMarket);
            for (Object networkName : jsonCurrency.keySet()) {
                int networkIdx = networksInfo.getIdx(networkName);

                JSONObject jsonNetwork = (JSONObject) jsonCurrency.get(networkName);
                processNetwork(jsonNetwork, currencyName, networkIdx, currencyIdx);
            }
        }
    }

    private void processNetwork(JSONObject jsonNetwork, String currencyName, int networkIdx, int currencyIdx) {
        if ((boolean) jsonNetwork.get("depositEnable") || (boolean) jsonNetwork.get("deposit_stopped")) {
            if (!deposit[networkIdx].contains(currencyName))
                deposit[networkIdx].put(currencyName, new LinkedList<>());
            deposit[networkIdx].get(currencyName).add(currencyIdx);
        }

        if ((boolean) jsonNetwork.get("withdrawEnable") || (boolean) jsonNetwork.get("withdraw_stopped")) {
            if (!withdrawal[networkIdx].contains(currencyName))
                withdrawal[networkIdx].put(currencyName, new LinkedList<>());
            withdrawal[networkIdx].get(currencyName).add(currencyIdx);
        }
    }

    private void createTransferList() {
        for (String networkName : networksInfo.listOfNetworks()) {
            int networkIdx = networksInfo.getIdx(networkName);
            if (withdrawal[networkIdx].size() > 0)
                createTransferEdges(networkName, networkIdx);
        }
    }

    private void createTransferEdges(String networkName, int networkIdx) {
        double percentCommission = 1, staticCommission;
        boolean available;

        for (String currencyName : withdrawal[networkIdx].keys()) {
            if (!deposit[networkIdx].contains(currencyName))
                continue;
            for (int withdrawalCurrency : withdrawal[networkIdx].get(currencyName)) {
                for (int depositCurrency : deposit[networkIdx].get(currencyName)) {
                    if (withdrawalCurrency == depositCurrency)
                        continue;

                    StockMarkets withdrawalMarket = currenciesInfo.getStockMarket(withdrawalCurrency);
                    StockMarkets depositMarket = currenciesInfo.getStockMarket(depositCurrency);
                    JSONObject withdrawalInfo = networksInfo.getNetworkInfo(withdrawalMarket, currencyName, networkName);
                    JSONObject depositInfo = networksInfo.getNetworkInfo(depositMarket, currencyName, networkName);


                    if (withdrawalInfo == null) {
                        System.out.printf("Hasn't found transfer info :: %s (%s)", currencyName, withdrawalMarket);
                        continue;
                    }
                    if (depositInfo == null) {
                        System.out.printf("Hasn't found transfer info :: %s (%s)", currencyName, depositMarket);
                        continue;
                    }

                    available = (boolean) depositInfo.get("depositEnable") && (boolean) withdrawalInfo.get("withdrawEnable");
                    staticCommission = Double.parseDouble((String) withdrawalInfo.get("withdrawFee"));

                    transferEdges.add(new DirectedEdge(withdrawalCurrency, depositCurrency, percentCommission,
                            staticCommission, networkName, !available));
                }
            }
        }
    }

    public Iterable<DirectedEdge> transferEdges() {
        return transferEdges;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

        System.out.println("Making a list of cryptocurrencies...");
        CurrenciesInfo currenciesInfo = new CurrenciesInfo(new ParsingInfo());
        System.out.println("Making transfer lists");
        TransferInfo transferInfo = new TransferInfo(currenciesInfo);


        String buffer = "";
        for (DirectedEdge edge : transferInfo.transferEdges()) {
            String from = currenciesInfo.getFullName(edge.from());
            String to = currenciesInfo.getFullName(edge.to());
            buffer += from + " -> " + to + " " + edge.marketName() + " " + edge.commission() + "\n";
        }

        File newFile = new File("./Transfers.txt");
        FileWriter writer = new FileWriter(newFile, false);
        writer.write(buffer);
        writer.close();
    }

}
