package no.ntnu.idatt2003.group38.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransactionFactory}.
 */
public class TransactionFactoryTest {
  private Share appleShare;

  @BeforeEach
  void setup() {
    Stock appleStock = new Stock("AAPL", "Apple", new BigDecimal("150"));
    appleShare = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));
  }

  // Positive tests

  @Test
  void returnsPurchaseInstance() {
    Transaction transaction = TransactionFactory.create(
        TransactionFactory.Type.Purchase, appleShare, 1);

    assertNotNull(transaction);
    assertInstanceOf(Purchase.class, transaction);
  }

  @Test
  void returnsSaleInstance() {
    Transaction transaction = TransactionFactory.create(
        TransactionFactory.Type.Sale, appleShare, 1);

    assertNotNull(transaction);
    assertInstanceOf(Sale.class, transaction);
  }

  @Test
  void setsShareCorrectly() {
    Transaction transaction = TransactionFactory.create(
        TransactionFactory.Type.Purchase, appleShare, 1);

    assertEquals(appleShare, transaction.getShare());
  }

  @Test
  void setsWeekCorrectly() {
    Transaction transaction = TransactionFactory.create(
        TransactionFactory.Type.Sale, appleShare, 5);

    assertEquals(5, transaction.getWeek());
  }

  @Test
  void returnsUncommittedTransaction() {
    Transaction transaction = TransactionFactory.create(
        TransactionFactory.Type.Purchase, appleShare, 1);

    assertFalse(transaction.isCommitted());
  }

  // Negative tests

  @Test
  void nullType_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () ->
        TransactionFactory.create(null, appleShare, 1));
  }

  @Test
  void nullShare_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () ->
        TransactionFactory.create(TransactionFactory.Type.Purchase, null, 1));
  }

  @Test
  void weekLessThanOne_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        TransactionFactory.create(TransactionFactory.Type.Sale, appleShare, 0));
  }
}
