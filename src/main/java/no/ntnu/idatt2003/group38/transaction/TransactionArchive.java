package no.ntnu.idatt2003.group38.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages a collection of financial transactions.
 *
 * <p>Class maintains an archive of all transactions (purchases and sales)
 * that a player has committed. It provides methods to retrieve transactions,
 * filter by type and week, and query archive metadata.
 */
public class TransactionArchive {
  private final List<Transaction> transactions;

  /**
   * Creates an empty transaction archive.
   */
  public TransactionArchive() {
    this.transactions = new ArrayList<>();
  }

  /**
   * Adds a transaction to the archive.
   *
   * @param transaction the transaction to add; must not be {@code null}
   * @return {@code true} if the transaction was successfully added; {@code false} otherwise
   * @throws NullPointerException if {@code transaction} is {@code null}
   */
  public boolean add(Transaction transaction) {
    Objects.requireNonNull(transaction, "transaction cannot be null");
    return this.transactions.add(transaction);
  }

  /**
   * Checks if the archive is empty.
   *
   * @return {@code true} if the archive contains no transactions; {@code false} otherwise
   */
  public boolean isEmpty() {
    return this.transactions.isEmpty();
  }

  /**
   * Returns all transactions in the archive.
   *
   * @return a list of all transactions in the archive
   */
  public List<Transaction> getTransactions() {
    return this.transactions;
  }

  /**
   * Retrieves all purchase transactions that occurred in the specified week.
   *
   * @param week the week number; must be ≥ 1
   * @return a list of all {@link Purchase} transactions for the given week
   * @throws IllegalArgumentException if {@code week} is less than 1
   */
  public List<Purchase> getPurchases(int week) {
    if (week < 1) {
      throw new IllegalArgumentException("week cannot be less than 1");
    }
    return transactions.stream()
        .filter(transaction -> transaction instanceof Purchase purchase
            && purchase.getWeek() == week)
        .map(t -> (Purchase) t)
        .toList();
  }

  /**
   * Retrieves all sale transactions that occurred in the specified week.
   *
   * @param week the week number; must be ≥ 1
   * @return a list of all {@link Sale} transactions for the given week
   * @throws IllegalArgumentException if {@code week} is less than 1
   */
  public List<Sale> getSales(int week) {
    if (week < 1) {
      throw new IllegalArgumentException("week cannot be less than 1");
    }
    return transactions.stream()
        .filter(transaction -> transaction instanceof Sale sale
            && sale.getWeek() == week)
        .map(t -> (Sale) t)
        .toList();
  }

  /**
   * Counts the number of distinct weeks in which transactions occurred.
   *
   * @return the number of distinct weeks represented in the archive
   */
  public int countDistinctWeeks() {
    return (int) this.transactions.stream()
        .map(Transaction::getWeek)
        .distinct()
        .count();
  }
}
