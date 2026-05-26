package no.ntnu.idatt2003.group38.filehandling;

import java.io.IOException;

/**
 * Thrown when a stock-data file contains malformed or semantically invalid content.
 */
public class InvalidStockDataException extends IOException {

  /**
   * Creates a new invalid-stock-data exception with the given message.
   *
   * @param message the validation error message
   */
  public InvalidStockDataException(String message) {
    super(message);
  }

  /**
   * Creates a new invalid-stock-data exception with the given message and cause.
   *
   * @param message the validation error message
   * @param cause the underlying cause
   */
  public InvalidStockDataException(String message, Throwable cause) {
    super(message, cause);
  }
}
