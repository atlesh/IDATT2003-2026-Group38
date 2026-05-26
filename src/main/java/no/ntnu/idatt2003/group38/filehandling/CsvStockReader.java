package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.model.Stock;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A {@link StockFileReader} implementation that reads stock data from a CSV file.
 *
 * <p>Rules applied during reading:</p>
 * <ul>
 *   <li>Lines starting with {@code #} are treated as comments and skipped.</li>
 *   <li>Blank or whitespace-only lines are skipped.</li>
 *   <li>Malformed stock rows cause an {@link InvalidStockDataException}.</li>
 *   <li>Duplicate stock symbols are rejected.</li>
 *   <li>A period ({@code .}) is used as the decimal separator for prices.</li>
 * </ul>
 */
public class CsvStockReader implements StockFileReader {

    private static final String COMMENT_PREFIX = "#";
    private static final String DELIMITER = ",";
    private static final int EXPECTED_FIELD_COUNT = 3;

    /**
     * Reads stocks from a CSV file at the given path.
     *
     * @param path the path to the CSV file; must not be {@code null}
     * @return a list of parsed {@link Stock} objects; never {@code null}
     * @throws IOException if the file cannot be read
     * @throws InvalidStockDataException if the file contains invalid stock rows
     * @throws NullPointerException if {@code path} is {@code null}
     */
    @Override
    public List<Stock> readStocks(Path path) throws IOException {
        Objects.requireNonNull(path, "Path cannot be null");

        List<Stock> stocks = new ArrayList<>();
        Set<String> seenSymbols = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                Stock stock = parseLine(trimmed, lineNumber);
                String normalizedSymbol = stock.getSymbol().toUpperCase(Locale.ROOT);
                if (!seenSymbols.add(normalizedSymbol)) {
                    throw new InvalidStockDataException(
                        "Duplicate stock symbol at line " + lineNumber + ": " + stock.getSymbol());
                }
                stocks.add(stock);
            }
        }

        return stocks;
    }

    /**
     * Attempts to parse a single CSV line into a {@link Stock}.
     *
     * @param line the trimmed, non-empty, non-comment line to parse
     * @param lineNumber the current file line number
     * @return a parsed {@link Stock}
     * @throws InvalidStockDataException if the line cannot be parsed into a valid stock
     */
    private Stock parseLine(String line, int lineNumber) throws InvalidStockDataException {
        String[] fields = line.split(DELIMITER, -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new InvalidStockDataException(
                "Invalid stock data at line " + lineNumber + ": expected 3 comma-separated fields");
        }

        String symbol = fields[0].trim();
        String name = fields[1].trim();
        String priceStr = fields[2].trim();

        if (symbol.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
            throw new InvalidStockDataException(
                "Invalid stock data at line " + lineNumber + ": symbol, company and price are required");
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidStockDataException(
                    "Invalid stock data at line " + lineNumber + ": price must be greater than 0");
            }
            return new Stock(symbol, name, price);
        } catch (NumberFormatException e) {
            throw new InvalidStockDataException(
                "Invalid stock data at line " + lineNumber + ": price must be numeric", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidStockDataException(
                "Invalid stock data at line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
