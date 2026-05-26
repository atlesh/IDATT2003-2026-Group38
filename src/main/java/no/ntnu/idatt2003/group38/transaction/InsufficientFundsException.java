package no.ntnu.idatt2003.group38.transaction;

/**
 * Thrown when a player attempts to commit a purchase without enough cash.
 */
public class InsufficientFundsException extends IllegalStateException {

  /**
   * Creates a new insufficient-funds exception with the given message.
   *
   * @param message the validation error message
   */
  public InsufficientFundsException(String message) {
    super(message);
  }
}
