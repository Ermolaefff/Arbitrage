package MONEYpackage.Parser;

import MONEYpackage.Algorithms.StringST;
import MONEYpackage.Algorithms.Union;
import MONEYpackage.Enums.StockMarkets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class NetworksInfo {
    private final JSONObject[] marketsJSONs = new JSONObject[StockMarkets.values().length];
    private final StringST<Integer> networksST;

    private final String dir = "./src/MONEYpackage/Parser/TransferInfo/StockMarkets/";
    private final String fileType = ".json";

    public NetworksInfo() throws IOException, ParseException {
        uniteNetworks();

        for (StockMarkets stockMarket : StockMarkets.values())
            marketsJSONs[stockMarket.ordinal()] = parseJSON(stockMarket);
        networksST = createNetworksST();
    }

    private void uniteNetworks() throws IOException {
        StringST<Integer> ST = createNetworksST();
        String[] networksNames = new String[ST.size()];

        for (String key : ST.keys())
            networksNames[ST.get(key)] = key;

        Union networksUnion = new Union(ST.size());

        networksUnion.unite(ST.get("ETH"), ST.get("ERC20"));
        networksUnion.unite(ST.get("ALGO"), ST.get("Algorand"));
        networksUnion.unite(ST.get("BSC"), ST.get("BEP20 (BSC)"));
        networksUnion.unite(ST.get("BSC"), ST.get("BEP20(BSC)"));
        networksUnion.unite(ST.get("SEGWITBTC"), ST.get("BTC-Segwit"));
        networksUnion.unite(ST.get("SOL"), ST.get("SPL(Solana)"));
        networksUnion.unite(ST.get("TRX"), ST.get("TRC20"));
        networksUnion.unite(ST.get("EGLD"), ST.get("egld"));

        for (StockMarkets stockMarket : StockMarkets.values())
            prepareJSON(stockMarket, ST, networksUnion, networksNames);
    }

    private StringST<Integer> createNetworksST() throws IOException {
        StringST<Integer> ST = new StringST<>();
        for (StockMarkets stockMarket : StockMarkets.values()) {

            String filePath = dir + stockMarket + fileType;
            FileReader reader = new FileReader(filePath);

            int numberOfUnclosedCases = 0;
            for (int c = reader.read(); c != -1; c = reader.read()) {
                char symbol = (char) c;
                if (symbol == '{')
                    numberOfUnclosedCases++;
                if (symbol == '}')
                    numberOfUnclosedCases--;

                if (numberOfUnclosedCases == 2 && symbol == '\"') {
                    String network = "";
                    while ((symbol = (char) reader.read()) != '\"')
                        network += symbol;
                   ST.put(network, ST.size());
                }
            }

            reader.close();
        }
        return ST;
    }

    private void prepareJSON(StockMarkets market, StringST<Integer> networksST, Union networksUnion, String[] networkName)
            throws IOException {
        String filePath = dir + market + fileType;
        FileReader reader = new FileReader(filePath);

        String buffer = "";
        int numberOfUnclosedCases = 0;
        for (int c = reader.read(); c != -1; c = reader.read()) {
            char symbol = (char) c;
            if (symbol == '{')
                numberOfUnclosedCases++;
            if (symbol == '}')
                numberOfUnclosedCases--;

            buffer += symbol;

            if (numberOfUnclosedCases == 2 && symbol == '\"') {
                String network = "";
                while ((symbol = (char) reader.read()) != '\"')
                    network += symbol;

                int networkIdx = networksST.get(network);
                int rootIdx = networksUnion.root(networkIdx);
                if (networkIdx != rootIdx)
                    network = networkName[rootIdx];

                buffer += network + "\"";
            }
        }

        reader.close();
        rewriteFile(filePath, buffer);
    }

    private void rewriteFile(String filePath, String content) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath, false));
        out.write(content);
        out.close();
    }
    private JSONObject parseJSON(StockMarkets market) throws IOException, ParseException {
        String filePath = dir + market + fileType;
        FileReader reader = new FileReader(filePath);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

        reader.close();

        return jsonObject.values().size() == 0 ? null : jsonObject;
    }
    public int numberOfNetworks() {
        return networksST.size();
    }
    public JSONObject getMarketInfo(StockMarkets stockMarket) {
        return marketsJSONs[stockMarket.ordinal()];
    }

    public JSONObject getCurrencyInfo(StockMarkets stockMarket, String currencyName) {
        return (JSONObject) marketsJSONs[stockMarket.ordinal()].get(currencyName);
    }

    public JSONObject getNetworkInfo(StockMarkets stockMarket, String currencyName, String networkName) {
        return (JSONObject) ((JSONObject) marketsJSONs[stockMarket.ordinal()].get(currencyName)).get(networkName);
    }

    public int getIdx(Object network) {
        return networksST.get(network);
    }

    public Iterable<String> listOfNetworks() {
        return networksST.keys();
    }

    public void showSuspiciousNetworks() throws IOException {
        StringST<Integer> networks = createNetworksST();
        for(String key : networks.keys()) {

            boolean hasLowercase = !key.equals(key.toUpperCase());
            boolean hasWhitespace = key.contains(" ");
            boolean hasNumbers = key.matches(".*\\d.*");
            boolean hasCases = key.contains("(") || key.contains(")") || key.contains("}") || key.contains("{");
            boolean hasOtherStaff = key.contains("-") || key.contains("/") || key.contains(",") || key.contains(":") || key.contains(".");

            if (hasLowercase || hasWhitespace || hasNumbers || hasCases || hasOtherStaff)
                System.out.println(key + " :: " + networks.get(key));
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        NetworksInfo networksInfo = new NetworksInfo();
        networksInfo.showSuspiciousNetworks();
    }
}
