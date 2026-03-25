package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.model.Stock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for writing stock data to persistent storage.
 */
public interface StockFileWriter {

    /**
     * Writes a list of stocks to the given file path.
     *
     * @param stocks the list of stocks to write. Must not be {@code null}
     * @param path the path to the file to write to. Must not be {@code null}
     * @throws IOException if an I/O error occurs while writing to the file
     * @throws NullPointerException if {@code stocks} or {@code path} is {@code null}
     */
    void writeStocks(List<Stock> stocks, Path path) throws IOException;
}
