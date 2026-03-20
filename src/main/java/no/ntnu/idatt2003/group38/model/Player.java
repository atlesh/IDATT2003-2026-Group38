package no.ntnu.idatt2003.group38.model;

import java.math.BigDecimal;
import java.util.Objects;
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
  }

  public String getName() {
    return this.name;
  }

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

  public Portfolio getPortfolio() {
    return this.portfolio;
  }


  public TransactionArchive getTransactionArchive() {
    return this.transactionArchive;
  }
}
