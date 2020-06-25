package csv;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV reader class which can read any valid file containing comma-separated values
 *
 * A header should be present on the first line of the file, else a {@link List} of columns
 * should be provided in the contructor or using the {@link CSVReader#setHeader(List)} method
 */
public class CSVReader {
    private List<String> header; // List of CSV columns to be used as keys
    private List<CSVEntry> entries = new ArrayList<>(); // List of entries successfully parsed

    public CSVReader(List<String> header) {
        this.header = header;
    }
    public CSVReader() {}

    /**
     * Read a CSV file from a reader, such as a {@link FileReader} or {@link InputStreamReader}
     *
     * Creates a {@link BufferedReader} from the provided {@link Reader}, reads the
     * first line into the header property if not already specified,
     * and then begins parsing the file.
     *
     * @param reader to read from
     */
    public void read(Reader reader) {
        try (BufferedReader br = new BufferedReader(reader)) {
            if (header == null) {
                header = Arrays.asList(br.readLine().split(", *"));
            }

            parse(br.lines().collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a CSV file stored locally
     * @param filename path to file to read
     * @throws FileNotFoundException if file is not found
     */
    public void read(String filename) throws FileNotFoundException {
        read(new FileReader(filename));
    }

    /**
     * Parse a {@link List} containing already read lines from a CSV file
     *
     * Iterates through the lines, split each line into an array of
     * columns, using the columns from the header List to store the
     * value from the current column into a {@link Map}, then create a
     * {@link CSVEntry} instance from that Map, which provides utility
     * methods, additional columns not specified in the header will
     * not be stored in the Map.
     *
     * @param lines List of comma-separated values
     */
    public void parse(List<String> lines) {
        entries.clear();
        lines.forEach(line -> {
            Map<String, String> map = new HashMap<>();

            String[] parts = line.split(", *");
            for (int i=0; i<parts.length && i<header.size(); i++) {
                map.putIfAbsent(header.get(i), parts[i]);
            }

            entries.add(new CSVEntry(map));
        });
    }

    /**
     * Set the CSV header for this {@link CSVReader}
     * @param header {@link List} of column headers
     */
    public void setHeader(List<String> header) {
        this.header = header;
    }

    /**
     * Get a {@link List} of parsed CSV entries
     * @return parsed entries
     */
    public List<CSVEntry> getEntries() {
        return entries;
    }

    /**
     * CSV class with utility methods such as converting
     * CSV entries into static types, as CSV files do not
     * have type definitions, provides conversions to
     * {@link String}, {@link Integer}, {@link Double} and {@link Date}
     */
    public static class CSVEntry {
        private Map<String, String> data;

        private CSVEntry() { this.data = new HashMap<>(); }

        CSVEntry(Map<String, String> data) { this.data = data; }

        /**
         * Retrieve {@link String} value from entry
         * @param key column name to lookup
         * @return value from CSV if found else default value
         */
        public String getString(String key) {
            return data.getOrDefault(key, "");
        }

        /**
         * Retrieve {@link Integer} value from entry
         *
         * Attempts to convert the stored value from {@link String}
         * to Integer, if not found returns zero
         *
         * @param key column name to lookup
         * @return value from CSV if found else default value
         */
        public Integer getInteger(String key) {
            try {
                return Integer.valueOf(getString(key));
            } catch (Exception ignored) {
                return 0;
            }
        }

        /**
         * Retrieve {@link Double} value from entry
         *
         * Attempts to convert the stored value from {@link String}
         * to Integer, if not found returns zero
         *
         * @param key column name to lookup
         * @return value from CSV if found else default value
         */
        public Double getDouble(String key) {
            try {
                return Double.valueOf(getString(key));
            } catch (Exception ignored) {
                return 0d;
            }
        }

        /**
         * Retrieve {@link Date} value from entry
         *
         * Attempts to convert the stored value from {@link String}
         * to Integer, if not found returns empty date
         *
         * @param key column name to lookup
         * @return value from CSV if found else default value
         */
        public LocalDate getDate(String key, DateTimeFormatter fmt) {
            try {
                return LocalDate.parse(getString(key), fmt);
            } catch (DateTimeParseException e) {
                return LocalDate.MIN;
            }
        }


        /**
         * Retrieve a {@link Set} containing all keys stored
         * in the entry
         * @return keys in set
         */
        public Set<String> keySet() {
            return data.keySet();
        }

        /**
         * @return string representation of all key/value pairs
         */
        @Override
        public String toString() {
            return "CSVEntry[" + keySet().stream().map(k -> String.format("%s=%s", k, getString(k)))
                    .collect(Collectors.joining(", ")) + "]";
        }
    }
}
