package no.ntnu.idatt2003.group38.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Share}.
 */
public class ShareTest {

  private Stock createStock() {
    return new Stock("AAPL", "Apple", new BigDecimal("150"));
  }

  // POSITIVE TESTS

  @Test
  void constructor_validInput_createsShare() {
    Share share = new Share(
        createStock(),
        new BigDecimal("10"),
        new BigDecimal("150")
    );

    assertEquals("AAPL", share.getStock().getSymbol());
    assertEquals(new BigDecimal("10"), share.getQuantity());
    assertEquals(new BigDecimal("150"), share.getPurchasePrice());
  }

  // NEGATIVE TESTS

  @Test
  void constructor_nullStock_throwsException() {
    assertThrows(NullPointerException.class, () ->
        new Share(null, new BigDecimal("10"), new BigDecimal("150"))
    );
  }

  @Test
  void constructor_nullQuantity_throwsException() {
    assertThrows(NullPointerException.class, () ->
        new Share(createStock(), null, new BigDecimal("150"))
    );
  }

  @Test
  void constructor_nullPrice_throwsException() {
    assertThrows(NullPointerException.class, () ->
        new Share(createStock(), new BigDecimal("10"), null)
    );
  }

  @Test
  void constructor_zeroQuantity_throwsException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Share(createStock(), BigDecimal.ZERO, new BigDecimal("150"))
    );
  }

  @Test
  void constructor_negativeQuantity_throwsException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Share(createStock(), new BigDecimal("-5"), new BigDecimal("150"))
    );
  }

  @Test
  void constructor_negativePrice_throwsException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Share(createStock(), new BigDecimal("5"), new BigDecimal("-10"))
    );
  }
}
