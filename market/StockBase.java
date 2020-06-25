package market;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

abstract class StockBase {
    static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-M-d");
    LocalDate date;

    StockBase() {
        this.date = LocalDate.MIN;
    }

    StockBase(LocalDate date) {
        this.date = date;
    }

    public static DateTimeFormatter getFmt() {
        return fmt;
    }

    public LocalDate getDate() {
        return date;
    }
}