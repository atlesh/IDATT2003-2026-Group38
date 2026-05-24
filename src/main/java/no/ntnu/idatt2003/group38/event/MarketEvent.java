package no.ntnu.idatt2003.group38.event;

import java.math.BigDecimal;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * Represents a random market event that can affect a stock's price during a
 * trading week.
 */
public interface MarketEvent {

  /**
   * Returns a short headline describing the event.
   *
   * @return the headline shown in the news dialog
   */
  String getHeadline();

  /**
   * Applies the event to the given stock and returns the new price.
   *
   * @param stock the affected stock. Must not be {@code null}
   * @param currentPrice the stock's current sales price. Must not be {@code null}
   * @return the new price after the event has been applied
   */
  BigDecimal apply(Stock stock, BigDecimal currentPrice);
}