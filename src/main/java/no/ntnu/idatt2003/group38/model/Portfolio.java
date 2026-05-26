package no.ntnu.idatt2003.group38.model;

import no.ntnu.idatt2003.group38.calculator.SaleCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class represents a portfolio of all the shares a user owns.
 */
public class Portfolio {

  private final List<Share> shares;

  /**
   * Creates a portfolio.
   */
  public Portfolio() {
    this.shares = new ArrayList<>();
  }

  /**
   * Method adds new share to portfolio if it doesn't already contain it.
   *
   * @param share the share to add
   * @return {@code true} if the share was added, {@code false} if it was already present
   * @throws NullPointerException if {@code share} is {@code null}
   */
  public boolean addShare(Share share) {
    Objects.requireNonNull(share, "share cannot be null");
    if (!this.shares.contains(share)) {
      this.shares.add(share);
      return true;
    }
    return false;
  }

  /**
   * Method removes chosen share if portfolio contains it.
   *
   * @param share the share to remove
   * @return {@code true} if the share was removed, {@code false} otherwise
   * @throws NullPointerException if {@code share} is {@code null}
   */
  public boolean removeShare(Share share) {
    Objects.requireNonNull(share, "share cannot be null");
    return this.shares.remove(share);
  }

  /**
   * Returns all shares currently in the portfolio.
   *
   * @return an unmodifiable snapshot of all shares in the portfolio
   */
  public List<Share> getShares() {
    return List.copyOf(this.shares);
  }

  /**
   * Method filters out a list with all shares of stocks with chosen symbol.
   *
   * @param symbol the stock symbol to filter by
   * @return a list of all shares whose stock matches the given symbol
   * @throws NullPointerException if {@code symbol} is {@code null}
   * @throws IllegalArgumentException if {@code symbol} is empty
   */
  public List<Share> getShares(String symbol) {
    Objects.requireNonNull(symbol, "symbol cannot be null");
    if (symbol.isEmpty()) {
      throw new IllegalArgumentException("symbol cannot be empty");
    }

    return this.shares.stream()
        .filter(share -> share.getStock().getSymbol().equals(symbol))
        .toList();
  }

  /**
   * Method checks if portfolio contains chosen share.
   *
   * @param share the share to check for
   * @return {@code true} if the portfolio contains the share, {@code false} otherwise
   * @throws NullPointerException if {@code share} is {@code null}
   */
  public boolean contains(Share share) {
    Objects.requireNonNull(share, "share cannot be null");
    return this.shares.contains(share);
  }

  /**
   * Returns the total net sale value of all shares in the portfolio.
   *
   * <p>The value is calculated using {@link SaleCalculator}
   * for each share and summed into one total.</p>
   *
   * @return the portfolio's total net worth
   */
  public BigDecimal getNetWorth() {
    BigDecimal total = BigDecimal.ZERO;

    for (Share share : this.shares) {
      total = total.add(new SaleCalculator(share).calculateTotal());
    }

    return total;
  }
}
