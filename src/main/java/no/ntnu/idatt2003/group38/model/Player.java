package no.ntnu.idatt2003.group38.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.idatt2003.group38.transaction.TransactionArchive;

/**
 * Represents a player in the game.
 *
 * <p>The player owns a {@link Portfolio}, a {@link TransactionArchive}
 * and a cash balance.  The balance can be increased or decreased with
 * {@link #addMoney(BigDecimal)} and {@link #withdrawMoney(BigDecimal)}.
 *
 * <p>All monetary values are stored as {@link BigDecimal} to avoid
 * rounding errors.
 */
public class Player {

  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;
  private final List<BigDecimal> netWorthHistory;

  /**
   * Creates a new player.
   *
   * @param name the name of the player; must not be {@code null}
   * @param startingMoney the initial amount of money; must not be {@code null}
   * @throws NullPointerException if {@code name} or {@code startingMoney} is {@code null}
   */
  public Player(String name, BigDecimal startingMoney) {
    this.name = Objects.requireNonNull(name, "name cannot be null");
    this.startingMoney = Objects.requireNonNull(
        startingMoney, "startingMoney cannot be null");
    this.money = this.startingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
    this.netWorthHistory = new ArrayList<>();
    this.netWorthHistory.add(this.startingMoney);
  }

  /**
   * Returns the player's name
   *
   * @return the player's name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the player's current cash balance
   *
   * @return the player's remaining money
   */
  public BigDecimal getMoney() {
    return this.money;
  }

  /**
   * Increases the player’s cash balance.
   *
   * @param amount the amount to add; must be non‑null and greater than zero
   * @throws NullPointerException     if {@code amount} is {@code null}
   * @throws IllegalArgumentException if {@code amount} is zero or negative
   */
  public void addMoney(BigDecimal amount) {
    Objects.requireNonNull(amount, "amount cannot be null");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Money cannot be negative");
    }
    this.money = this.money.add(amount);
  }

  /**
   * Decreases the player’s cash balance.
   *
   * @param amount the amount to withdraw; must be non‑null and greater than zero
   * @throws NullPointerException     if {@code amount} is {@code null}
   * @throws IllegalArgumentException if {@code amount} is zero or negative
   */
  public void withdrawMoney(BigDecimal amount) {
    Objects.requireNonNull(amount, "amount cannot be null");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Money cannot be negative");
    }
    this.money = this.money.subtract(amount);
  }

  /**
   * Returns the player's portfolio
   *
   * @return the player's portfolio
   */
  public Portfolio getPortfolio() {
    return this.portfolio;
  }

  public List<BigDecimal> getNetWorthHistory() {
    return List.copyOf(this.netWorthHistory);
  }

  public void recordNetWorthSnapshot() {
    this.netWorthHistory.add(getNetWorth());
  }

  /**
   * Returns the player's transaction archive
   *
   * @return the player's transaction archive
   */
  public TransactionArchive getTransactionArchive() {
    return this.transactionArchive;
  }

  /**
   * Returns the player's total net worth
   *
   * <p>This includes the player's current cash balance and the net sale value
   * of all shares in the player's portfolio</p>
   *
   * @return the player's total net worth
   */
  public BigDecimal getNetWorth() {
    return this.money.add(this.portfolio.getNetWorth());
  }

  /**
   * Returns the player's current status based on trading activity and net worth growth.
   *
   * <ul>
   *   <li><b>Novice</b>: starting level, no requirements</li>
   *   <li><b>Investor</b>: traded in at least 10 weeks and net worth has grown by at least 20%</li>
   *   <li><b>Speculator</b>: traded in at least 20 weeks and net worth has at least doubled</li>
   * </ul>
   *
   * @return the player's current status as a {@link String}
   */
  public String getPlayerStatus() {
    int weeksTraded = this.transactionArchive.countDistinctWeeks();
    BigDecimal gain = getNetWorth().divide(this.startingMoney, 10, RoundingMode.HALF_UP);

    if (weeksTraded >= 20 && gain.compareTo(new BigDecimal("2.0")) >= 0) {
      return "Speculator";
    } else if (weeksTraded >= 10 && gain.compareTo(new BigDecimal("1.2")) >= 0) {
      return "Investor";
    } else {
      return "Novice";
    }
  }
}
