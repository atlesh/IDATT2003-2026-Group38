package no.ntnu.idatt2003.group38.transaction;

import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Represents a purchase of a {@link Share}.
 *
 * <p>The transaction uses a {@link PurchaseCalculator}
 * to compute the gross price, commission and total cost.
 */
public class Purchase extends Transaction {

  /**
   * Creates a purchase transaction for the given share in the specified week.
   *
   * @param share the share to be bought; must not be {@code null}
   * @param week  the week number when the purchase occurs; must be >= 1
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
  }

  /**
   * Commits the purchase:
   * <ul>
   *   <li>Verifies the transaction has not already been committed.</li>
   *   <li>Checks the player has sufficient funds (using {@link Player#getMoney()}
   *       and the calculator’s {@code calculateTotal()}).</li>
   *   <li>Deducts the total cost from the player’s money via {@link Player#withdrawMoney(java.math.BigDecimal)}.</li>
   *   <li>Adds the purchased share to the player’s portfolio.</li>
   *   <li>Adds the transaction to the player’s transactionArchive</li>
   *   <li>Marks the transaction as committed.</li>
   * </ul>
   *
   * @param player the player performing the purchase; must not be {@code null}
   * @throws IllegalStateException    if the transaction is already committed
   * @throws InsufficientFundsException if the player lacks sufficient funds
   * @throws NullPointerException     if {@code player} is {@code null}
   */
  @Override
  public void commit(Player player) {
    if (isCommitted()) {
      throw new IllegalStateException("Transaction already committed");
    }

    var totalCost = getCalculator().calculateTotal();
    if (player.getMoney().compareTo(totalCost) < 0) {
      throw new InsufficientFundsException(
          "Insufficient funds: player has " + player.getMoney()
              + " but purchase costs " + totalCost);
    }

    player.withdrawMoney(totalCost);

    player.getPortfolio().addShare(getShare());

    player.getTransactionArchive().add(this);

    setCommitted();
  }

  private Purchase(Share share, int week, boolean committed) {
    super(share, week, new PurchaseCalculator(share));
    if (committed) {
      setCommitted();
    }
  }

  /**
   * Restores a committed purchase from saved game data.
   *
   * @param share the share that was bought
   * @param week the week in which the purchase occurred
   * @return a committed purchase representing the saved transaction
   */
  public static Purchase restore(Share share, int week) {
    return new Purchase(share, week, true);
  }
}
