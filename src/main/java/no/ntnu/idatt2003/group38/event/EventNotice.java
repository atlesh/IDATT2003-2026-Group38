package no.ntnu.idatt2003.group38.event;

import java.math.BigDecimal;
import java.util.Objects;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * Immutable record describing a market event that fired during a trading week.
 *
 * <p>Exposed by {@code Exchange.getLastWeekEvents()} so the GUI can present a
 * news summary after the player advances to a new week.</p>
 *
 * @param stock         the stock that was affected
 * @param headline      the headline describing the event
 * @param previousPrice the price before the event was applied
 * @param newPrice      the price after the event was applied
 */
public record EventNotice(
    Stock stock,
    String headline,
    BigDecimal previousPrice,
    BigDecimal newPrice) {

  /**
   * Compact constructor that validates all components are non-null.
   */
  public EventNotice {
    Objects.requireNonNull(stock, "stock cannot be null");
    Objects.requireNonNull(headline, "headline cannot be null");
    Objects.requireNonNull(previousPrice, "previousPrice cannot be null");
    Objects.requireNonNull(newPrice, "newPrice cannot be null");
  }
}
