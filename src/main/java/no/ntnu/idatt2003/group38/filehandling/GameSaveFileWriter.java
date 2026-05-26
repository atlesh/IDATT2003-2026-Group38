package no.ntnu.idatt2003.group38.filehandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Writes complete game-save snapshots to disk as JSON.
 */
public class GameSaveFileWriter {

  private final ObjectMapper objectMapper;

  /**
   * Creates a new file writer for game-save snapshots.
   */
  public GameSaveFileWriter() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new Jdk8Module());
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Writes the current game state to the given file path.
   *
   * @param path     the file to write to
   * @param player   the player to save
   * @param exchange the exchange to save
   * @throws IOException if the file cannot be written
   */
  public void write(Path path, Player player, Exchange exchange) throws IOException {
    Objects.requireNonNull(path, "Path cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Objects.requireNonNull(exchange, "Exchange cannot be null");

    GameSave save = GameSaveMapper.toSave(player, exchange);

    Path parent = path.getParent();

    if (parent != null) {
      Files.createDirectories(parent);
    }

    this.objectMapper.writeValue(path.toFile(), save);
  }
}
