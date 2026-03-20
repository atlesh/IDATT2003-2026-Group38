package no.ntnu.idatt2003.group38.transaction;

import java.util.Objects;

import no.ntnu.idatt2003.group38.calculator.TransactionCalculator;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Abstract base class for all financial transactions that a {@link Player}
 * can perform.
 *
 * <p>Each transaction holds the {@link Share} being transacted, the week in which the
 * transaction occurs, and a {@link TransactionCalculator} that knows how to compute
 * gross value, commission, tax and total value for the specific transaction type.
 *
 */
public abstract class Transaction {
  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;
  private boolean committed;

  /**
   * Creates a new {@code Transaction}.
   *
   * @param share      the {@link Share} involved in the transaction; must not be {@code null}
   * @param week       the week number for the transaction; must be ≥ 1
   * @param calculator the calculator that knows how to evaluate this transaction; must not be {@code null}
   * @throws NullPointerException     if {@code share} or {@code calculator} is {@code null}
   * @throws IllegalArgumentException if {@code week} is less than 1
   */
  protected Transaction(Share share, int week, TransactionCalculator calculator) {
    this.share = Objects.requireNonNull(share, "share cannot be null");
    this.calculator = Objects.requireNonNull(calculator, "calculator cannot be null");
    if (week < 1) {
      throw new IllegalArgumentException("week must be ≥ 1");
    }
    this.week = week;
    this.committed = false;
  }

  public Share getShare() {
    return this.share;
  }

  public int getWeek() {
    return this.week;
  }

  public TransactionCalculator getCalculator() {
    return this.calculator;
  }

  public boolean isCommitted() {
    return this.committed;
  }

  /** Marks the transaction as having been successfully committed. */
  protected void setCommitted() {
    this.committed = true;
  }

  /**
   * Executes the transaction, updating the {@link Player}'s
   * (money, portfolio, etc.) and persisting the transaction in the player's
   * {@link TransactionArchive}.
   *
   * @param player the player that performs the transaction; must not be {@code null}
   * @throws IllegalStateException if the transaction has already been committed
   */
  public abstract void commit(Player player);
}
