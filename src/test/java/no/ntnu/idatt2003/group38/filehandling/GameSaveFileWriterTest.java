package no.ntnu.idatt2003.group38.filehandling;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
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
 * Unit tests for {@link GameSaveFileWriter}.
 */
public class GameSaveFileWriterTest {

  @TempDir
  Path tempDir;

  private GameSaveFileWriter writer;
  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock appleStock = new Stock("AAPL", "Apple", new BigDecimal("150.00"));
    Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));

    this.exchange = new Exchange("Test Exchange", List.of(appleStock, microsoftStock));
    this.player = new Player("Jeff", new BigDecimal("10000.00"));
    this.writer = new GameSaveFileWriter();

    this.exchange.buy("AAPL", new BigDecimal("10"), this.player);
    this.exchange.advance();
    this.player.recordNetWorthSnapshot();
  }

  @Test
  void write_validPath_createsJsonFile() throws IOException {
    Path path = this.tempDir.resolve("save.json");

    this.writer.write(path, this.player, this.exchange);

    assertTrue(Files.exists(path));

    String content = Files.readString(path);

    assertTrue(content.contains("Jeff"));
    assertTrue(content.contains("exchangeName"));
    assertTrue(content.contains("Test Exchange"));
  }

  @Test
  void write_nestedPath_createsParentDirectories() throws IOException {
    Path path = this.tempDir.resolve("saves").resolve("manual").resolve("save.json");

    this.writer.write(path, this.player, this.exchange);

    assertTrue(Files.exists(path));
    assertTrue(Files.isDirectory(path.getParent()));
  }

  @Test
  void write_nullArguments_throwsException() {
    Path path = this.tempDir.resolve("save.json");

    assertThrows(NullPointerException.class,
        () -> this.writer.write(null, this.player, this.exchange));
    assertThrows(NullPointerException.class, () -> this.writer.write(path, null, this.exchange));
    assertThrows(NullPointerException.class, () -> this.writer.write(path, this.player, null));
  }
}
