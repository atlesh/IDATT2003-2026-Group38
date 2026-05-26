package no.ntnu.idatt2003.group38.filehandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Reads complete game-save snapshots from JSON files.
 */
public class GameSaveFileReader {

  private final ObjectMapper objectMapper;

  /**
   * Creates a new file reader for game-save snapshots.
   */
  public GameSaveFileReader() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new Jdk8Module());
  }

  /**
   * Reads a saved game from the given file path.
   *
   * @param path the file to read from
   * @return the reconstructed loaded game
   * @throws IOException if the file cannot be read
   */
  public LoadedGame read(Path path) throws IOException {
    Objects.requireNonNull(path, "Path cannot be null");

    if (!Files.exists(path) || !Files.isRegularFile(path)) {
      throw new IOException("Save file does not exist: " + path);
    }

    GameSave save = this.objectMapper.readValue(path.toFile(), GameSave.class);

    return GameSaveMapper.fromSave(save);
  }
}
