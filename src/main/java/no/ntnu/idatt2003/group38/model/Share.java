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
   * Creates the share from the stock, the quantity and the price.
   *
   * @param stock stock that was bought.
   * @param quantity how much was bought.
   * @param purchasePrice the price of the purchase.
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

  public Stock getStock() {
    return stock;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }
}