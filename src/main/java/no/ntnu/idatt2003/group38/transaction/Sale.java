package no.ntnu.idatt2003.group38.transaction;

import java.math.BigDecimal;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;

/**
 * Represents a sale of a {@link Share}.
 *
 * <p>The transaction uses a {@link SaleCalculator}
 * to compute the gross proceeds, commission, tax and the net amount
 * that the player will receive.
 */
public class Sale extends Transaction {

  private final Share sourceShare;

  /**
   * Creates a sale transaction for the given share in the specified week.
   *
   * @param share the share to be sold; must not be {@code null}
   * @param week  the week number when the sale occurs; must be >= 1
   */
  public Sale(Share share, int week) {
    this(share, share, week);
  }

  /**
   * Creates a sale transaction for the given share in the specified week.
   *
   * @param soldShare   the quantity being sold
   * @param sourceShare the original share lot in the portfolio
   * @param week        the week number when the sale occurs; Must be >= 1
   */
  public Sale(Share soldShare, Share sourceShare, int week) {
    super(soldShare, week, new SaleCalculator(soldShare));
    this.sourceShare = sourceShare;
  }

  private Sale(Share soldShare, Share sourceShare, int week, BigDecimal salesPrice,
               boolean committed) {
    super(soldShare, week, new SaleCalculator(soldShare, salesPrice));
    this.sourceShare = sourceShare;
    if (committed) {
      setCommitted();
    }
  }

  /**
   * Commits the sale.
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
   * @throws IllegalStateException if the transaction is already committed
   *                               or the player does not own the share
   * @throws NullPointerException  if {@code player} is {@code null}
   */
  @Override
  public void commit(Player player) {
    if (isCommitted()) {
      throw new IllegalStateException("Transaction already committed");
    }

    if (!player.getPortfolio().contains(this.sourceShare)) {
      throw new IllegalStateException("Player does not own the share to be sold");
    }

    player.getPortfolio().removeShare(this.sourceShare);

    BigDecimal remainingQuantity =
        this.sourceShare.getQuantity().subtract(getShare().getQuantity());
    if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
      Share remainder = new Share(
          this.sourceShare.getStock(),
          remainingQuantity,
          this.sourceShare.getPurchasePrice());
      player.getPortfolio().addShare(remainder);
    }

    player.addMoney(getCalculator().calculateTotal());
    player.getTransactionArchive().add(this);
    setCommitted();
  }

  /**
   * Restores a committed sale from saved game data.
   *
   * @param soldShare  the share quantity that was sold
   * @param week       the week in which the sale occurred
   * @param salesPrice the historical sale price to calculate the transaction from
   * @return a committed sale representing the saved transaction
   */
  public static Sale restore(Share soldShare, int week, BigDecimal salesPrice) {
    return new Sale(soldShare, soldShare, week, salesPrice, true);
  }
}
