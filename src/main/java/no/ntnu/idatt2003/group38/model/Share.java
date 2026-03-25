package no.ntnu.idatt2003.group38.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Class represents the share you end up with after a purchase.
 */
public class Share {
  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  /**
   * Creates a new share.
   *
   * @param stock the stock that was bought; must not be {@code null}
   * @param quantity the quantity bought; must be greater than 0
   * @param purchasePrice the purchase price per unit; must not be {@code null} or negative
   * @throws NullPointerException if {@code stock}, {@code quantity},
   *                              or {@code purchasePrice} is {@code null}
   * @throws IllegalArgumentException if {@code quantity} is less than or equal to 0,
   *                                  or if {@code purchasePrice} is negative
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
    this.stock = Objects.requireNonNull(stock, "stock cannot be null");
    this.quantity = Objects.requireNonNull(quantity, "quantity cannot be null");
    this.purchasePrice = Objects.requireNonNull(purchasePrice, "purchasePrice cannot be null");

    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Purchase price cannot be negative");
    }
  }

  /**
   * Returns the stock associated with this share
   *
   * @return the stock
   */
  public Stock getStock() {
    return this.stock;
  }

  /**
   * Returns the quantity of the share
   *
   * @return the quantity bought
   */
  public BigDecimal getQuantity() {
    return this.quantity;
  }

  /**
   * Returns the purchase price per unit
   *
   * @return the purchase price
   */
  public BigDecimal getPurchasePrice() {
    return this.purchasePrice;
  }
}