package no.ntnu.idatt2003.group38.filehandling;

import java.math.BigDecimal;
import java.util.List;

/**
 * Serializable snapshot of an entire game session.
 *
 * @param exchangeName the name of the exchange
 * @param week         the current trading week
 * @param player       the saved player state
 * @param stocks       the saved stocks on the exchange
 */
public record GameSave(
    String exchangeName,
    int week,
    SavedPlayer player,
    List<SavedStock> stocks
) {

  /**
   * Serializable snapshot of the player state.
   *
   * @param name            the player name
   * @param startingMoney   the player's original starting capital
   * @param money           the player's current cash balance
   * @param netWorthHistory the saved net-worth history
   * @param portfolio       the saved share lots currently held
   * @param transactions    the saved transaction history
   */
  public record SavedPlayer(
      String name,
      BigDecimal startingMoney,
      BigDecimal money,
      List<BigDecimal> netWorthHistory,
      List<SavedShare> portfolio,
      List<SavedTransaction> transactions) {
  }

  /**
   * Serializable snapshot of a stock and its price history.
   *
   * @param stockSymbol      the stock ticker symbol
   * @param company          the company name
   * @param historicalPrices the saved sales-price history
   */
  public record SavedStock(
      String stockSymbol,
      String company,
      List<BigDecimal> historicalPrices) {
  }

  /**
   * Serializable snapshot of a share lot in the player's portfolio.
   *
   * @param stockSymbol   the symbol of the stock
   * @param quantity      the quantity held in this lot
   * @param purchasePrice the original purchase price per share
   */
  public record SavedShare(
      String stockSymbol,
      BigDecimal quantity,
      BigDecimal purchasePrice) {

  }

  /**
   * Supported transaction kinds in saved game data.
   */
  public enum TransactionType {
    /**
     * A saved purchase transaction.
     */
    PURCHASE,
    /**
     * A saved sale transaction.
     */
    SALE
  }

  /**
   * Serializable snapshot of a transaction.
   *
   * @param type          the type of transaction
   * @param stockSymbol   the symbol of the transacted stock
   * @param quantity      the quantity that was bought or sold
   * @param purchasePrice the original purchase price per share
   * @param salePrice     the sale price per share, used only for saved sales
   * @param week          the week in which the transaction occurred
   */
  public record SavedTransaction(
      TransactionType type,
      String stockSymbol,
      BigDecimal quantity,
      BigDecimal purchasePrice,
      BigDecimal salePrice,
      int week) {

  }
}
