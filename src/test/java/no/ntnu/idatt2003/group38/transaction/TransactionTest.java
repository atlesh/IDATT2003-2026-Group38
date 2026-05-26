package no.ntnu.idatt2003.group38.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Transaction}.
 */
public class TransactionTest {
  private Share appleShare;
  private Player player;

  @BeforeEach
  void setup() {
    Stock appleStock = new Stock("AAPL", "Apple", new BigDecimal("150"));
    appleShare = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));
    player = new Player("John Doe", new BigDecimal("5000"));
  }

  // POSITIVE TESTS

  @Test
  void getShare_returnsCorrectShare() {
    Purchase purchase = new Purchase(appleShare, 1);
    assertEquals(appleShare, purchase.getShare());
  }

  @Test
  void getWeek_returnsCorrectWeek() {
    Purchase purchase = new Purchase(appleShare, 5);
    assertEquals(5, purchase.getWeek());
  }

  @Test
  void getCalculator_returnsCalculator() {
    Purchase purchase = new Purchase(appleShare, 1);
    assertNotNull(purchase.getCalculator());
  }

  @Test
  void isCommitted_initiallyFalse() {
    Purchase purchase = new Purchase(appleShare, 1);
    assertFalse(purchase.isCommitted());
  }

  @Test
  void isCommitted_trueAfterCommit() {
    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(player);
    assertTrue(purchase.isCommitted());
  }

  // NEGATIVE TESTS

  @Test
  void constructor_nullShare_throwsException() {
    assertThrows(NullPointerException.class, () -> new Purchase(null, 1));
  }

  @Test
  void constructor_weekLessThanOne_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> new Purchase(appleShare, 0));
  }

  @Test
  void constructor_negativeWeek_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> new Purchase(appleShare, -5));
  }
}
