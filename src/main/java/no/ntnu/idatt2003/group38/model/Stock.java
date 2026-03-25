package no.ntnu.idatt2003.group38.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stock listed on the exchange
 *
 * <p>A stock has a symbol, a company name and a history of sales prices</p>
 */
public class Stock {

  private final String symbol;
  private final String company;
  private final List<BigDecimal> prices;

  /**
   * Creates a new stock.
   *
   * @param symbol the stock ticker symbol; must not be {@code null} or blank
   * @param company the company name. Must not be {@code null} or blank
   * @param salesPrices the initial sales price. Must not be {@code null}
   * @throws NullPointerException if anything is {@code null}
   * @throws IllegalArgumentException if {@code symbol} or {@code company} is blank
   */
  public Stock(String symbol, String company, BigDecimal salesPrices) {
    this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
    if (symbol.isBlank()) {
      throw new IllegalArgumentException("Symbol cannot be blank");
    }

    this.company = Objects.requireNonNull(company, "company cannot be null");
    if (company.isBlank()) {
      throw new IllegalArgumentException("Company cannot be blank");
    }
    Objects.requireNonNull(salesPrices, "salesPrice cannot be null");

    this.prices = new ArrayList<>();
    this.prices.add(salesPrices);
  }

  /**
   * Returns the stock symbol.
   *
   * @return the symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the company name.
   *
   * @return the company name
   */
  public String getCompany() {
    return company;
  }

  /**
   * Returns the current sales price.
   *
   * @return the current sales price
   */
  public BigDecimal getSalesPrice() {
    return prices.getLast();
  }

  /**
   * Registers a new sales price for the stock.
   *
   * @param price the new price to add. Must not be {@code null}
   * @throws NullPointerException if {@code price} is {@code null}
   */
  public void addNewSalesPrice(BigDecimal price) {
    Objects.requireNonNull(price, "price cannot be null");
    prices.add(price);
  }

  /**
   * Returns the price history of this stock.
   *
   * @return an unmodifiable list of all registered prices. Never {@code null}
   */
  public List<BigDecimal> getHistoricalPrices() {
    return Collections.unmodifiableList(prices);
  }

  /**
   * Returns the highest price ever registered for this stock.
   *
   * @return the highest price
   */
  public BigDecimal getHighestPrice() {
    return prices.stream()
        .max(BigDecimal::compareTo)
        .orElseThrow();
  }

  /**
   * Returns the lowest price ever registered for this stock.
   *
   * @return the lowest price
   */
  public BigDecimal getLowestPrice() {
    return prices.stream()
        .min(BigDecimal::compareTo)
        .orElseThrow();
  }

  /**
   * Returns the price change between the last and second-to-last registered price.
   *
   * <p>If only one price has been registered, this is interpreted as no change
   * and zero is returned.</p>
   *
   * @return the difference between the latest and previous price.
   */
  public BigDecimal getLatestPriceChange() {
    if (prices.size() < 2) {
      return BigDecimal.ZERO;
    }
    BigDecimal latest = prices.getLast();
    BigDecimal previous = prices.get(prices.size() - 2);
    return latest.subtract(previous);
  }
}