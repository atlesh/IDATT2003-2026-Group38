package no.ntnu.idatt2003.group38.event;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * Positive single-stock event. Boosts the affected stock's price by 6 % - 20 %.
 */
public class EarningsBeatEvent implements MarketEvent {

  private final Random random;

  /**
   * Creates a new earnings-beat event.
   *
   * @param random the source of randomness.
   * @throws NullPointerException if {@code random} is {@code null}
   */
  public EarningsBeatEvent(Random random) {
    this.random = Objects.requireNonNull(random, "random cannot be null");
  }

  /** {@inheritDoc} */
  @Override
  public String getHeadline() {
    return "Has risen significantly in the last week";
  }

  /** {@inheritDoc} */
  @Override
  public BigDecimal apply(Stock stock, BigDecimal currentPrice) {
    Objects.requireNonNull(stock, "stock cannot be null");
    Objects.requireNonNull(currentPrice, "currentPrice cannot be null");

    double boost = 0.06 + (this.random.nextDouble() * 0.14);
    BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(boost));
    return currentPrice.multiply(factor);
  }
}
