package no.ntnu.idatt2003.group38.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;

import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RumorGeneratorTest {

  private List<Stock> stocks;

  @BeforeEach
  void setUp() {
    stocks = List.of(
        new Stock("AAPL", "Apple", new BigDecimal("100")),
        new Stock("GOOG", "Google", new BigDecimal("200")),
        new Stock("MSFT", "Microsoft", new BigDecimal("300")),
        new Stock("TSLA", "Tesla", new BigDecimal("400"))
    );
  }

  // POSITIVE TESTS

  @Test
  void generate_zeroChance_neverProducesRumors() {
    RumorGenerator generator = new RumorGenerator(new Random(42), 0.65, 2, 0.0);

    for (int i = 0; i < 100; i++) {
      assertTrue(generator.generate(stocks).isEmpty());
    }
  }

  @Test
  void generate_fullChance_alwaysProducesRumors() {
    RumorGenerator generator = new RumorGenerator(new Random(42), 0.65, 2, 1.0);

    for (int i = 0; i < 100; i++) {
      List<Rumor> rumors = generator.generate(stocks);
      assertFalse(rumors.isEmpty());
      assertTrue(rumors.size() <= 2);
    }
  }

  @Test
  void generate_rumorsPointAtDistinctStocks() {
    RumorGenerator generator = new RumorGenerator(new Random(42), 0.65, 3, 1.0);

    for (int i = 0; i < 100; i++) {
      List<Rumor> rumors = generator.generate(stocks);
      Set<String> symbols = new java.util.HashSet<>();
      for (Rumor rumor : rumors) {
        assertTrue(symbols.add(rumor.stock().getSymbol()),
            "Duplicate stock in rumor batch");
      }
    }
  }

  @Test
  void generate_emptyStocks_returnsEmpty() {
    RumorGenerator generator = new RumorGenerator(new Random(42), 0.65, 2, 1.0);
    assertTrue(generator.generate(List.of()).isEmpty());
  }

  @Test
  void generate_accuracyAffectsTruthfulRatio() {
    // With accuracy = 1.0, every rumor should be truthful.
    RumorGenerator alwaysTruthful = new RumorGenerator(new Random(42), 1.0, 2, 1.0);
    for (int i = 0; i < 100; i++) {
      for (Rumor rumor : alwaysTruthful.generate(stocks)) {
        assertTrue(rumor.truthful());
      }
    }

    // With accuracy = 0.0, every rumor should be untruthful.
    RumorGenerator alwaysLying = new RumorGenerator(new Random(42), 0.0, 2, 1.0);
    for (int i = 0; i < 100; i++) {
      for (Rumor rumor : alwaysLying.generate(stocks)) {
        assertFalse(rumor.truthful());
      }
    }
  }

  @Test
  void rumor_headline_containsStockSymbol() {
    Rumor rumor = new Rumor(stocks.getFirst(), Rumor.Direction.RISE, true);
    assertTrue(rumor.getHeadline().contains("AAPL"));
  }

  // NEGATIVE TESTS

  @Test
  void constructor_nullRandom_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new RumorGenerator(null));
  }

  @Test
  void constructor_accuracyOutOfRange_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new RumorGenerator(new Random(), -0.1, 2, 0.5));
    assertThrows(IllegalArgumentException.class,
        () -> new RumorGenerator(new Random(), 1.1, 2, 0.5));
  }

  @Test
  void constructor_negativeMaxRumors_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new RumorGenerator(new Random(), 0.5, -1, 0.5));
  }

  @Test
  void constructor_rumorChanceOutOfRange_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new RumorGenerator(new Random(), 0.5, 2, -0.1));
    assertThrows(IllegalArgumentException.class,
        () -> new RumorGenerator(new Random(), 0.5, 2, 1.1));
  }

  @Test
  void generate_nullStocks_throwsException() {
    RumorGenerator generator = new RumorGenerator(new Random(42));
    assertThrows(NullPointerException.class,
        () -> generator.generate(null));
  }

  @Test
  void rumor_nullStock_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Rumor(null, Rumor.Direction.RISE, true));
  }

  @Test
  void rumor_nullDirection_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Rumor(stocks.getFirst(), null, true));
  }
}