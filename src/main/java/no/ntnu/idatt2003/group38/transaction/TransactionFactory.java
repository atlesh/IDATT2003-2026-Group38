package no.ntnu.idatt2003.group38.transaction;

import no.ntnu.idatt2003.group38.model.Share;

/**
 * Factory for creating a {@link Transaction}.
 *
 * <p>Lets the caller choose between creating a {@link Purchase} and a {@link Sale}.
 */
public final class TransactionFactory {

  /**
   * Enumerates the supported transaction types.
   */
  public enum Type {
    /** Creates a purchase transaction. */
    Purchase,
    /** Creates a sale transaction. */
    Sale
  }

  private TransactionFactory() {
  }

  /**
   * Creates a new {@link Transaction} of the specified type.
   *
   * <p>Validation of {@code share} and {@code week} is delegated to the
   * underlying {@link Transaction} constructor.
   *
   * @param type the type of transaction to create; must not be {@code null}
   * @param share the share involved in the transaction; must not be {@code null}
   * @param week the week number when the transaction occurs; must be ≥ 1
   * @return a new {@link Purchase} or {@link Sale} corresponding to {@code type}
   * @throws NullPointerException if {@code type} or {@code share} is {@code null}
   * @throws IllegalArgumentException if {@code week} is less than 1
   */
  public static Transaction create(Type type, Share share, int week) {
    if (type == null) {
      throw new NullPointerException("type cannot be null");
    }
    return switch (type) {
      case Purchase -> new Purchase(share, week);
      case Sale -> new Sale(share, week);
    };
  }
}
