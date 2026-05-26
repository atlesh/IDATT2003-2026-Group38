package no.ntnu.idatt2003.group38.filehandling;

/**
 * Thrown when saved game data is structurally valid JSON but semantically invalid.
 */
public class InvalidSaveFileException extends IllegalArgumentException {

  /**
   * Creates a new invalid-save exception with the given message.
   *
   * @param message the validation error message
   */
  public InvalidSaveFileException(String message) {
    super(message);
  }

  /**
   * Creates a new invalid-save exception with the given message and cause.
   *
   * @param message the validation error message
   * @param cause the underlying cause
   */
  public InvalidSaveFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
