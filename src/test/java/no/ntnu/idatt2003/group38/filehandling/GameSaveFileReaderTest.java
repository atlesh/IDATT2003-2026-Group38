package no.ntnu.idatt2003.group38.filehandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link GameSaveFileReader}.
 */
public class GameSaveFileReaderTest {

  @TempDir
  Path tempDir;

  private GameSaveFileWriter writer;
  private GameSaveFileReader reader;
  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock appleStock = new Stock("AAPL", "Apple", new BigDecimal("150.00"));
    Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));

    this.exchange = new Exchange("Test Exchange", List.of(appleStock, microsoftStock));
    this.player = new Player("Jeff", new BigDecimal("10000.00"));

    this.writer = new GameSaveFileWriter();
    this.reader = new GameSaveFileReader();

    this.exchange.buy("AAPL", new BigDecimal("10"), this.player);
    this.exchange.buy("MSFT", new BigDecimal("5"), this.player);
    this.exchange.advance();
    this.player.recordNetWorthSnapshot();
  }

  @Test
  void read_existingSaveFile_restoresGameState() throws IOException {
    Path path = this.tempDir.resolve("save.json");

    this.writer.write(path, this.player, this.exchange);
    LoadedGame loadedGame = this.reader.read(path);

    assertEquals(this.player.getName(), loadedGame.player().getName());
    assertEquals(this.exchange.getWeek(), loadedGame.exchange().getWeek());
    assertEquals(
        this.player.getTransactionArchive().getTransactions().size(),
        loadedGame.player().getTransactionArchive().getTransactions().size()
    );
    assertEquals(
        this.player.getNetWorthHistory().size(),
        loadedGame.player().getNetWorthHistory().size()
    );
  }

  @Test
  void read_missingFile_throwsIOException() {
    Path missing = this.tempDir.resolve("missing.json");

    assertThrows(IOException.class, () -> this.reader.read(missing));
  }

  @Test
  void read_invalidJson_throwsIOException() throws IOException {
    Path path = this.tempDir.resolve("invalid.json");
    Files.writeString(path, "{ invalid json }", StandardCharsets.UTF_8);

    assertThrows(IOException.class, () -> this.reader.read(path));
  }

  @Test
  void read_nullPath_throwsException() {
    assertThrows(NullPointerException.class, () -> this.reader.read(null));
  }
}
