package market;

import csv.CSVReader;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Stock class used to represent stock from a single public trading company
 *
 * Constructor accepts a ticker symbol as available on
 * https://iextrading.com/apps/stocks
 */
public class Stock {
    private String symbol;
    private List<StockItem> history = new ArrayList<>();

    public Stock(String symbol) {
        try {
            this.symbol = URLEncoder.encode(symbol, "utf-8");
        } catch (UnsupportedEncodingException e) {
            this.symbol = "";
        }
    }

    /**
     * Retrieve a month of stocks value history
     *
     * Requests 30 days of history from iextrading.com for this company,
     * loads HTTP response into an InputStreamReader which is passed to {@link CSVReader},
     * entries are returned as a List of {@link StockItem}
     *
     * @return List of stock history objects
     */
    public List<StockItem> getMonth() {
        try {
            URL endpoint = new URL("https://api.iextrading.com/1.0/stock/" + symbol + "/chart/1M?format=csv");
            URLConnection uc = endpoint.openConnection();

            CSVReader reader = new CSVReader();
            reader.read(new InputStreamReader(uc.getInputStream()));

            reader.getEntries().forEach(e -> history.add(new StockItem(e).symbol(symbol)));
        } catch (Exception ignored) {
        }

        return history();
    }

    public List<StockItem> history() {
        return history;
    }

    public String symbol() {
        return symbol;
    }
}
