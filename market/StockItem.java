package market;

import csv.CSVReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Stock history item, contains stock value and trade details for a company
 * at a point in time.
 */
public class StockItem extends StockBase {
    private int numberOfTrades, volume;
    private double high, low, average, notional, open, close, changeOverTime, change;
    private String label, symbol;

    private StockItem() {
        super();
        this.high = 0;
        this.low = 0;
        this.average = 0;
        this.volume = 0;
        this.notional = 0;
        this.numberOfTrades = 0;
        this.open = 0;
        this.close = 0;
        this.changeOverTime = 0;
        this.change = 0;
        this.symbol = "";
    }

    public StockItem(LocalDate date, String label, double high, double low, double average, int volume, double notional,
                     int numberOfTrades, double open, double close,
                     double changeOverTime, double change, String symbol) {
        super();

        this.date = date;
        this.label = label;
        this.high = high;
        this.low = low;
        this.average = average;
        this.volume = volume;
        this.notional = notional;
        this.numberOfTrades = numberOfTrades;
        this.open = open;
        this.close = close;
        this.changeOverTime = changeOverTime;
        this.change = change;
        this.symbol = symbol;
    }

    /**
     * Read values from a {@link CSVReader.CSVEntry} into new instance
     * @param entry CSV entry
     */
    StockItem(CSVReader.CSVEntry entry) {
        this();

        date = entry.getDate("date", fmt);
        label = entry.getString("label");
        open = entry.getDouble("open");
        high = entry.getDouble("high");
        low = entry.getDouble("low");
        close = entry.getDouble("close");
        volume = entry.getInteger("volume");
        change = entry.getDouble("change");
        changeOverTime = entry.getDouble("changeOverTime");
    }

    StockItem symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    /**
     * Read values from a line of comma-separated values
     * @param row comma-separated values
     */
    @Deprecated
    public StockItem(String row) {
        this();

        String[] parts = row.split(",");
        if (parts.length != 12)
            return;

        LinkedList<String> a = new LinkedList<>(Arrays.asList(row.split(",")));

        try {
            date = LocalDate.parse(a.remove(), fmt);
        } catch (DateTimeParseException e) {
            date = LocalDate.MIN;
        }

        try {
            open           = Double  .parseDouble (a.remove());
            high           = Double  .parseDouble (a.remove());
            low            = Double  .parseDouble (a.remove());
            close          = Double  .parseDouble (a.remove());
            volume         = Integer .parseInt    (a.remove()); a.remove();
            change         = Double  .parseDouble (a.remove()); a.remove(); a.remove(); a.remove();
            changeOverTime = Double  .parseDouble (a.remove());
        } catch (Exception ignored) {
        }
    }

    /**
     * @return string representation of most important values
     */
    @Override
    public String toString() {
        return String.format("StockItem[label=%s, date=%s, high=%,.2f, low=%,.2f, volume=%,d]",
                label, date, high, low, volume);
    }

//    /**
//     * Test CSV retrieval and parsing
//     */
//    public static void main(String[] args) {
//        List<Stock> inventory = new ArrayList<>();
//        String[] symbols = {"TSLA", "GOOGL", "AAPL"};
//
//        for (String symbol : symbols) {
//            Stock s = new Stock(symbol);
//            inventory.add(s);
//        }
//
//        for (Stock stock : inventory) {
//            System.out.println(stock.symbol());
//            stock.getMonth().subList(0, Math.min(3, stock.history().size())).forEach(System.out::println);
//        }
//    }

    // region Getters
    public static DateTimeFormatter getFmt() {
        return fmt;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getNumberOfTrades() {
        return numberOfTrades;
    }

    public int getVolume() {
        return volume;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getAverage() {
        return average;
    }

    public double getNotional() {
        return notional;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getChangeOverTime() {
        return changeOverTime;
    }

    public double getChange() {
        return change;
    }

    public String getLabel() {
        return label;
    }
    // endregion
}