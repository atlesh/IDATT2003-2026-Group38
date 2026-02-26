package no.ntnu.idatt2003.group38;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class represents a portfolio of all the shares a user owns.
 */
public class Portfolio {

  private List<Share> shares;

  /**
   * Creates a portfolio.
   */
  public Portfolio() {
    this.shares = new ArrayList<>();
  }

  /**
   * Method adds new share to portfolio if it doesnt already contain it.
   *
   * @param share
   * @return True if added successfully, False if not.
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
   * @param share
   * @return True if removed successfully, False if not.
   */
  public boolean removeShare(Share share) {
    Objects.requireNonNull(share, "share cannot be null");
    return this.shares.remove(share);
  }

  public List<Share> getShares() {
    return this.shares;
  }

  /**
   * Method filters out a list with all shares of stocks with chosen symbol.
   *
   * @param symbol
   * @return List with all shares of stocks with symbol.
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
   * @param share
   * @return True if portfolio contains Share, False if not.
   */
  public boolean contains(Share share) {
    Objects.requireNonNull(share, "share cannot be null");
    return this.shares.contains(share);
  }
}
