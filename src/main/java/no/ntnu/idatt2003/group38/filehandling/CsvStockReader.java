package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.model.Stock;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link StockFileReader} implementation that reads stock data from a CSV file.
 *
 * <p>Rules applied during reading:</p>
 * <ul>
 *   <li>Lines starting with {@code #} are treated as comments and skipped.</li>
 *   <li>Blank or whitespace-only lines are skipped.</li>
 *   <li>Lines that are not the expected format are skipped silently.</li>
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
     * @throws NullPointerException if {@code path} is {@code null}
     */
    @Override
    public List<Stock> readStocks(Path path) throws IOException {
        Objects.requireNonNull(path, "Path cannot be null");

        List<Stock> stocks = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                Stock stock = parseLine(trimmed);
                if (stock != null) {
                    stocks.add(stock);
                }
            }
        }

        return stocks;
    }

    /**
     * Attempts to parse a single CSV line into a {@link Stock}.
     *
     * @param line the trimmed, non-empty, non-comment line to parse
     * @return a {@link Stock} if parsing succeeds, or {@code null} otherwise
     */
    private Stock parseLine(String line) {
        String[] fields = line.split(DELIMITER, -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            return null;
        }

        String symbol = fields[0].trim();
        String name = fields[1].trim();
        String priceStr = fields[2].trim();

        if (symbol.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
            return null;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return new Stock(symbol, name, price);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
