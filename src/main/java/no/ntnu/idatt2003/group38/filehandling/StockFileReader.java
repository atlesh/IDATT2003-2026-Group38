package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.model.Stock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for reading stock data from persistent storage.
 */
public interface StockFileReader {

    /**
     * Reads a list of stocks from the given file path.
     *
     * @param path the path to the file to read from. Must not be {@code null}
     * @return a list of {@link Stock} objects read from the file. Can never be {@code null}
     * @throws IOException if an I/O error occurs while reading the file
     * @throws IllegalArgumentException if {@code path} is {@code null}
     */
    List<Stock> readStocks(Path path) throws IOException;
}
