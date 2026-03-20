package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvStockReader}.
 */
class CsvStockReaderTest {

    private CsvStockReader reader;

    @BeforeEach
    void setUp() {
        reader = new CsvStockReader();
    }

    // POSITIVE TESTS

    @Test
    void returnsCorrectStocks(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("stocks.csv");
        Files.writeString(file,
            """
                # Top stocks
                NVDA,Nvidia,191.27
                AAPL,Apple Inc.,276.43
                """,
                StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertEquals(2, stocks.size());
        assertEquals("NVDA", stocks.getFirst().getSymbol());
        assertEquals("Nvidia", stocks.get(0).getCompany());
        assertEquals(0, stocks.get(0).getSalesPrice().compareTo(
            new BigDecimal("191.27")));
        assertEquals("AAPL", stocks.get(1).getSymbol());
    }

    @Test
    void skipsCommentLines(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("stocks.csv");
        Files.writeString(file,
            """
                # This is a comment
                # Another comment
                TSLA,Tesla,250.00
                """,
                StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertEquals(1, stocks.size());
        assertEquals("TSLA", stocks.getFirst().getSymbol());
    }

    @Test
    void skipsBlankLines(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("stocks.csv");
        Files.writeString(file,
            """
                
                MSFT,Microsoft,404.68
                
                  \s
                """,
                StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertEquals(1, stocks.size());
        assertEquals("MSFT", stocks.getFirst().getSymbol());
    }

    @Test
    void emptyFile_returnsEmptyList(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("empty.csv");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void onlyCommentsAndBlanks_returnsEmptyList(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("comments.csv");
        Files.writeString(file, "# comment\n\n# another\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void skipsMalformedLines(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("mixed.csv");
        Files.writeString(file,
            """
                AAPL,Apple Inc.,150.00
                this line is bad
                GOOG,Alphabet,175.50
                """,
                StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertEquals(2, stocks.size());
        assertEquals("AAPL", stocks.get(0).getSymbol());
        assertEquals("GOOG", stocks.get(1).getSymbol());
    }

    @Test
    void parsedCorrectly(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("decimal.csv");
        Files.writeString(file, "AMZN,Amazon,188.40\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertEquals(1, stocks.size());
        assertEquals(0, stocks.getFirst().getSalesPrice().compareTo(
            new BigDecimal("188.40")));
    }

    // NEGATIVE TESTS

    @Test
    void nullPath_throwsException() {
        assertThrows(NullPointerException.class, () -> reader.readStocks(null));
    }

    @Test
    void nonExistentFile_throwsIOException(@TempDir Path dir) {
        Path missing = dir.resolve("does_not_exist.csv");
        assertThrows(IOException.class, () -> reader.readStocks(missing));
    }

    @Test
    void negativePrice_isSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("negative.csv");
        Files.writeString(file, "BAD,BadCorp,-10.00\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void zeroPrice_isSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("zero.csv");
        Files.writeString(file, "ZER,ZeroCorp,0.00\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void nonNumericPrice_isSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("nan.csv");
        Files.writeString(file, "BAD,BadCorp,abc\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void tooFewFields_isSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("toofew.csv");
        Files.writeString(file, "AAPL,Apple Inc.\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void tooManyFields_isSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("toomany.csv");
        Files.writeString(file, "AAPL,Apple Inc.,150.00,extrafield\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void blankSymbol_isSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("blanksym.csv");
        Files.writeString(file, ",Apple Inc.,150.00\n", StandardCharsets.UTF_8);

        List<Stock> stocks = reader.readStocks(file);

        assertTrue(stocks.isEmpty());
    }
}
