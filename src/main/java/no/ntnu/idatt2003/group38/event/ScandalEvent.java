package no.ntnu.idatt2003.group38.event;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * Negative single-stock event. Drops the affected stock's price by 20 % – 30 %.
 */
public class ScandalEvent implements MarketEvent {

  private final Random random;

  /**
   * Creates a new scandal event.
   *
   * @param random the source of randomness.
   * @throws NullPointerException if {@code random} is {@code null}
   */
  public ScandalEvent(Random random) {
    this.random = Objects.requireNonNull(random, "random cannot be null");
  }

  @Override
  public String getHeadline() {
    return "Company scandal rocks investors";
  }

  @Override
  public BigDecimal apply(Stock stock, BigDecimal currentPrice) {
    Objects.requireNonNull(stock, "stock cannot be null");
    Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

    // Drop between 20 % and 30 %.
    double drop = 0.20 + (this.random.nextDouble() * 0.10);
    BigDecimal factor = BigDecimal.ONE.subtract(BigDecimal.valueOf(drop));
    return currentPrice.multiply(factor);
  }
}
