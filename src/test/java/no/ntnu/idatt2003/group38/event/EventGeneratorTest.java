package no.ntnu.idatt2003.group38.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventGeneratorTest {

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple", new BigDecimal("100"));
  }

  // POSITIVE TESTS

  @Test
  void sample_zeroChance_alwaysReturnsEmpty() {
    EventGenerator generator = new EventGenerator(
        new Random(42), 0.0, List.of(EarningsBeatEvent::new));

    for (int i = 0; i < 1000; i++) {
      assertTrue(generator.sample().isEmpty());
    }
  }

  @Test
  void sample_fullChance_alwaysReturnsEvent() {
    EventGenerator generator = new EventGenerator(
        new Random(42), 1.0, List.of(EarningsBeatEvent::new));

    for (int i = 0; i < 100; i++) {
      assertTrue(generator.sample().isPresent());
    }
  }

  @Test
  void sample_lowChance_firesRarely() {
    EventGenerator generator = new EventGenerator(
        new Random(42), 0.02, List.of(EarningsBeatEvent::new));

    int fired = 0;
    for (int i = 0; i < 10_000; i++) {
      if (generator.sample().isPresent()) {
        fired++;
      }
    }
    // With p = 0.02 over 10_000 trials, expected ≈ 200.
    // Allow generous tolerance for the seeded RNG.
    assertTrue(fired > 100 && fired < 350,
        "Expected ~200 fires, got " + fired);
  }

  @Test
  void earningsBeat_increasesPrice() {
    EarningsBeatEvent event = new EarningsBeatEvent(new Random(42));
    BigDecimal newPrice = event.apply(stock, new BigDecimal("100"));

    // Between +15 % and +25 %.
    assertTrue(newPrice.compareTo(new BigDecimal("115.00")) >= 0);
    assertTrue(newPrice.compareTo(new BigDecimal("125.00")) <= 0);
  }

  @Test
  void scandal_decreasesPrice() {
    ScandalEvent event = new ScandalEvent(new Random(42));
    BigDecimal newPrice = event.apply(stock, new BigDecimal("100"));

    // Between -20 % and -30 %.
    assertTrue(newPrice.compareTo(new BigDecimal("70.00")) >= 0);
    assertTrue(newPrice.compareTo(new BigDecimal("80.00")) <= 0);
  }

  // NEGATIVE TESTS

  @Test
  void constructor_nullRandom_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new EventGenerator(null));
  }

  @Test
  void constructor_negativeChance_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new EventGenerator(new Random(), -0.1, List.of(EarningsBeatEvent::new)));
  }

  @Test
  void constructor_chanceGreaterThanOne_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new EventGenerator(new Random(), 1.1, List.of(EarningsBeatEvent::new)));
  }

  @Test
  void constructor_emptyFactories_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new EventGenerator(new Random(), 0.5, List.of()));
  }

  @Test
  void earningsBeat_nullStock_throwsException() {
    EarningsBeatEvent event = new EarningsBeatEvent(new Random());
    assertThrows(NullPointerException.class,
        () -> event.apply(null, new BigDecimal("100")));
  }

  @Test
  void earningsBeat_nullPrice_throwsException() {
    EarningsBeatEvent event = new EarningsBeatEvent(new Random());
    assertThrows(NullPointerException.class,
        () -> event.apply(stock, null));
  }
}
