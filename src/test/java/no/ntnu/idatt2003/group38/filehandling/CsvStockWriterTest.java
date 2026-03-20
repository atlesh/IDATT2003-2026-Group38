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
 * Unit tests for {@link CsvStockWriter}.
 */
class CsvStockWriterTest {

    private CsvStockWriter writer;

    @BeforeEach
    void setUp() {
        writer = new CsvStockWriter();
    }

    // POSITIVE TESTS

    @Test
    void writesHeaderAndData(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("out.csv");
        List<Stock> stocks = List.of(
                new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00")),
                new Stock("MSFT", "Microsoft", new BigDecimal("404.68"))
        );

        writer.writeStocks(stocks, file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertTrue(
            lines.getFirst().startsWith("#"), "First line should be a comment header");
        assertTrue(lines.stream().anyMatch(l -> l.contains("AAPL")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("MSFT")));
    }

    @Test
    void emptyList_writesOnlyHeader(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("empty_out.csv");

        writer.writeStocks(List.of(), file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
        assertTrue(lines.getFirst().startsWith("#"));
    }

    @Test
    void createsFileIfNotExists(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("new_file.csv");
        assertFalse(Files.exists(file));

        writer.writeStocks(List.of(new Stock("X", "XCorp",
            new BigDecimal("10.00"))), file);

        assertTrue(Files.exists(file));
    }

    @Test
    void overwritesExistingContent(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("overwrite.csv");
        Files.writeString(file, "old content", StandardCharsets.UTF_8);

        writer.writeStocks(List.of(new Stock("NEW", "NewCorp",
            new BigDecimal("5.00"))), file);

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertFalse(content.contains("old content"));
        assertTrue(content.contains("NEW"));
    }

    @Test
    void correctLineFormat(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("format.csv");

        writer.writeStocks(List.of(new Stock("TSLA", "Tesla",
            new BigDecimal("250.50"))), file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertTrue(lines.stream().anyMatch(l -> l.equals("TSLA,Tesla,250.50")));
    }

    @Test
    void roundTripWithReader(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("roundtrip.csv");
        List<Stock> original = List.of(
                new Stock("NVDA", "Nvidia", new BigDecimal("191.27")),
                new Stock("AAPL", "Apple Inc.", new BigDecimal("276.43"))
        );

        writer.writeStocks(original, file);
        List<Stock> loaded = new CsvStockReader().readStocks(file);

        assertEquals(2, loaded.size());
        assertEquals("NVDA", loaded.getFirst().getSymbol());
        assertEquals("Nvidia", loaded.get(0).getCompany());
        assertEquals(0, loaded.get(0).getSalesPrice().compareTo(
            new BigDecimal("191.27")));
        assertEquals("AAPL", loaded.get(1).getSymbol());
    }

    // NEGATIVE TESTS

    @Test
    void nullPath_throwsException() {
        assertThrows(NullPointerException.class,
                () -> writer.writeStocks(List.of(), null));
    }

    @Test
    void nullStocksList_throwsException(@TempDir Path dir) {
        Path file = dir.resolve("out.csv");
        assertThrows(NullPointerException.class,
                () -> writer.writeStocks(null, file));
    }
}
