package MONEYpackage.Analisis;

import java.io.*;

public class LogsAnalyzer {
    private static String dir = "./src/MONEYpackage/";

    public static void main(String[] args) throws IOException {
        Writer writer = new FileWriter(dir + "Logs2.txt");

        BufferedReader reader = new BufferedReader(new FileReader(dir + "Logs.txt"));
        for(String profitStr = reader.readLine(); profitStr != null; profitStr = reader.readLine()) {
            String durationStr = reader.readLine();
            double duration = Double.parseDouble(durationStr.split(":: ")[1]);
            if (duration >= 10) {
                writer.write(profitStr + "\n");
                writer.write(durationStr + "\n");
            }

        }
        writer.close();
    }
}
