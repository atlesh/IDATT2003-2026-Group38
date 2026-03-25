package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.model.Stock;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A {@link StockFileWriter} that writes stock data to a CSV file.
 *
 * <p>Each stock is written as one line in the format:</p>
 * <pre>
 *   symbol,name,price
 * </pre>
 *
 * <p>Rules applied during writing:</p>
 * <ul>
 *   <li>A header comment is written at the top of the file.</li>
 *   <li>The price written is the stock current sales price.</li>
 *   <li>The file is created if it does not exist, or overwritten if it does.</li>
 * </ul>
 */
public class CsvStockWriter implements StockFileWriter {

    private static final String DELIMITER = ",";
    private static final String FILE_HEADER = "# symbol,name,price";

    /**
     * Writes a list of stocks to a CSV file at the given path.
     *
     * @param stocks the list of stocks to write. Must not be {@code null}
     * @param path   the path to write to. Must not be {@code null}
     * @throws IOException if the file cannot be written
     * @throws NullPointerException if {@code stocks} or {@code path} is {@code null}
     */
    @Override
    public void writeStocks(List<Stock> stocks, Path path) throws IOException {
        Objects.requireNonNull(stocks, "Stocks cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(FILE_HEADER);
            writer.newLine();

            for (Stock stock : stocks) {
                writer.write(formatLine(stock));
                writer.newLine();
            }
        }
    }

    /**
     * Formats a {@link Stock} as a CSV line: {@code symbol,name,price}.
     *
     * @param stock the stock to format. Must not be {@code null}
     * @return the formatted CSV line
     */
    private String formatLine(Stock stock) {
        return stock.getSymbol()
                + DELIMITER
                + stock.getCompany()
                + DELIMITER
                + stock.getSalesPrice().toPlainString();
    }
}
