package no.ntnu.idatt2003.group38.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RumorGenerator}.
 */
public class RumorGeneratorTest {

  @Test
  void generate_zeroMaxRumorsPerWeek_returnsEmptyList() {
    RumorGenerator generator = new RumorGenerator(new Random(42), 0.65, 0, 1.0);
    List<Stock> stocks = List.of(
        new Stock("AAPL", "Apple", new BigDecimal("100.00")),
        new Stock("MSFT", "Microsoft", new BigDecimal("200.00"))
    );

    List<Rumor> rumors = generator.generate(stocks);

    assertTrue(rumors.isEmpty());
  }

  @Test
  void generate_withPositiveMaxRumors_returnsNoMoreThanConfiguredMaximum() {
    RumorGenerator generator = new RumorGenerator(new Random(42), 0.65, 2, 1.0);
    List<Stock> stocks = List.of(
        new Stock("AAPL", "Apple", new BigDecimal("100.00")),
        new Stock("MSFT", "Microsoft", new BigDecimal("200.00")),
        new Stock("NVDA", "Nvidia", new BigDecimal("300.00"))
    );

    List<Rumor> rumors = generator.generate(stocks);

    assertTrue(!rumors.isEmpty());
    assertTrue(rumors.size() <= 2);
  }

  @Test
  void constructor_negativeMaxRumorsPerWeek_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> new RumorGenerator(new Random(), 0.65, -1, 1.0));
  }
}
