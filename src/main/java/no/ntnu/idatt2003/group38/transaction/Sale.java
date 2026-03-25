package no.ntnu.idatt2003.group38.transaction;

import no.ntnu.idatt2003.group38.calculator.SaleCalculator;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Represents a sale of a {@link Share}.
 *
 * <p>The transaction uses a {@link SaleCalculator}
 * to compute the gross proceeds, commission, tax and the net amount
 * that the player will receive.
 */
public class Sale extends Transaction {

  /**
   * Creates a sale transaction for the given share in the specified week.
   *
   * @param share the share to be sold; must not be {@code null}
   * @param week  the week number when the sale occurs; Must be >= 1
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));
  }

  /**
   * Commits the sale:
   * <ul>
   *   <li>Ensures the transaction has not been committed before.</li>
   *   <li>Verifies the player actually owns the share being sold.</li>
   *   <li>Calculates the net proceeds via {@code calculateTotal()}.</li>
   *   <li>Removes the share from the player's portfolio.</li>
   *   <li>Adds the net proceeds to the player's money.</li>
   *   <li>Adds the transaction to the player’s transactionArchive</li>
   *   <li>Marks the transaction as committed.</li>
   * </ul>
   *
   * @param player the player performing the sale; must not be {@code null}
   * @throws IllegalStateException    if the transaction is already committed or the player does not own the share
   * @throws NullPointerException     if {@code player} is {@code null}
   */
  @Override
  public void commit(Player player) {
    if (isCommitted()) {
      throw new IllegalStateException("Transaction already committed");
    }

    if (!player.getPortfolio().contains(getShare())) {
      throw new IllegalStateException("Player does not own the share to be sold");
    }

    player.getPortfolio().removeShare(getShare());

    player.addMoney(getCalculator().calculateTotal());

    player.getTransactionArchive().add(this);

    setCommitted();
  }
}
