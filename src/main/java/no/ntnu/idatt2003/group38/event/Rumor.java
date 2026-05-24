package no.ntnu.idatt2003.group38.event;

import java.util.Objects;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * A rumor about the future direction of a stock's price.
 *
 * <p>Rumors are surfaced to the player after a week advance as
 * speculative tips alongside the week's news. They predict whether a
 * stock is likely to rise or fall in the upcoming week, but they are
 * not always correct — see {@link RumorGenerator} for the accuracy model.</p>
 *
 * @param stock the stock the rumor is about
 * @param direction the predicted direction of the next price move
 * @param truthful whether the rumor is internally flagged as truthful;
 *     used by {@link RumorGenerator} to bias next week's price movement.
 *     This field is intentionally not exposed to the player — they have
 *     to decide for themselves whether to trust the rumor.
 */
public record Rumor(Stock stock, Direction direction, boolean truthful) {

  /**
   * Predicted direction of a stock's next price move.
   */
  public enum Direction {
    /** The rumor predicts a price rise. */
    RISE,
    /** The rumor predicts a price fall. */
    FALL
  }

  /**
   * Compact constructor validating all components.
   */
  public Rumor {
    Objects.requireNonNull(stock, "stock cannot be null");
    Objects.requireNonNull(direction, "direction cannot be null");
  }

  /**
   * Returns a short, user-facing headline describing the rumor.
   *
   * @return the headline shown in the news dialog
   */
  public String getHeadline() {
    return switch (direction) {
      case RISE -> "Whispers suggest " + stock.getSymbol() + " is set to rise";
      case FALL -> "Insiders fear " + stock.getSymbol() + " may drop";
    };
  }
}