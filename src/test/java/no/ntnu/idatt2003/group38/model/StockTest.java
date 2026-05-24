package no.ntnu.idatt2003.group38.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Unit tests for {@link Stock}.
 */
public class StockTest {

  @Test
  void constructor_setsFields_andInitialSalesPrice() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("123.45"));

    assertEquals("AAPL", stock.getSymbol());
    assertEquals("Apple Inc", stock.getCompany());
    assertEquals(new BigDecimal("123.45"), stock.getSalesPrice());
  }

  @Test
  void getSalesPrice_returnsLastAddedPrice() {
    Stock stock = new Stock("TSLA", "Tesla", new BigDecimal("100.00"));

    stock.addNewSalesPrice(new BigDecimal("101.50"));
    stock.addNewSalesPrice(new BigDecimal("99.99"));

    assertEquals(new BigDecimal("99.99"), stock.getSalesPrice());
  }

  // getPrices

  @Test
  void initiallyOnlyStartPrice() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

    List<BigDecimal> prices = stock.getHistoricalPrices();

    assertEquals(1, prices.size());
    assertEquals(new BigDecimal("100.00"), prices.getFirst());
  }

  @Test
  void containsAllRegisteredPrices() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("110.00"));
    stock.addNewSalesPrice(new BigDecimal("120.00"));

    List<BigDecimal> prices = stock.getHistoricalPrices();

    assertEquals(3, prices.size());
    assertEquals(new BigDecimal("100.00"), prices.get(0));
    assertEquals(new BigDecimal("110.00"), prices.get(1));
    assertEquals(new BigDecimal("120.00"), prices.get(2));
  }

  @Test
  void returnsUnmodifiableList() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

    assertThrows(UnsupportedOperationException.class,
        () -> stock.getHistoricalPrices().add(new BigDecimal("200.00")));
  }

  // getHighestPrice

  @Test
  void singlePrice_returnsThatAsHighest() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

    assertEquals(new BigDecimal("100.00"), stock.getHighestPrice());
  }

  @Test
  void multiplePrices_returnsHighest() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    stock.addNewSalesPrice(new BigDecimal("80.00"));
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    stock.addNewSalesPrice(new BigDecimal("120.00"));

    assertEquals(new BigDecimal("200.00"), stock.getHighestPrice());
  }

  // getLowestPrice

  @Test
  void singlePrice_returnsThatAsLowest() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

    assertEquals(new BigDecimal("100.00"), stock.getLowestPrice());
  }

  @Test
  void multiplePrices_returnsLowest() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    stock.addNewSalesPrice(new BigDecimal("40.00"));
    stock.addNewSalesPrice(new BigDecimal("200.00"));

    assertEquals(new BigDecimal("40.00"), stock.getLowestPrice());
  }

  // getPriceChange

  @Test
  void singlePrice_returnsZero() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

    assertEquals(0, stock.getLatestPriceChange().compareTo(BigDecimal.ZERO));
  }

  @Test
  void priceIncreased_returnsPositiveDifference() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("120.00"));

    assertEquals(0, stock.getLatestPriceChange().compareTo(new BigDecimal("20.00")));
  }

  @Test
  void priceDecreased_returnsNegativeDifference() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("80.00"));

    assertEquals(0, stock.getLatestPriceChange().compareTo(new BigDecimal("-20.00")));
  }

  @Test
  void onlyLastTwoPrices() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    stock.addNewSalesPrice(new BigDecimal("210.00"));

    assertEquals(0, stock.getLatestPriceChange().compareTo(new BigDecimal("10.00")));
  }

  // Constructor negative tests

  @Test
  void constructor_nullSymbol_throwsNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new Stock(null, "Apple Inc", new BigDecimal("100.00")));
  }

  @Test
  void constructor_blankSymbol_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("", "Apple Inc", new BigDecimal("100.00")));
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("   ", "Apple Inc", new BigDecimal("100.00")));
  }

  @Test
  void constructor_nullCompany_throwsNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new Stock("SYM", null, new BigDecimal("10.00")));
  }

  @Test
  void constructor_blankCompany_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("SYM", "", new BigDecimal("10.00")));
    assertThrows(IllegalArgumentException.class,
        () -> new Stock("SYM", "  ", new BigDecimal("10.00")));
  }

  @Test
  void constructor_nullSalesPrice_throwsNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> new Stock("SYM", "Company", null));
  }

  @Test
  void addNewSalesPrice_null_throwsNullPointerException() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

    assertThrows(NullPointerException.class,
        () -> stock.addNewSalesPrice(null));
  }

  // LatestPriceChangePercent
  @Test
  void returnsPositivePercent() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("110.00"));
    // (110 - 100) / 100 * 100 = 10.00
    assertEquals(0, stock.getLatestPriceChangePercent().compareTo(new BigDecimal("10.00")));
  }

  @Test
  void returnsNegativePercent() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("200.00"));
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    // (150 - 200) / 200 * 100 = -25.00
    assertEquals(0, stock.getLatestPriceChangePercent().compareTo(new BigDecimal("-25.00")));
  }

  @Test
  void singlePrice_percentReturnsZero() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    assertEquals(0, stock.getLatestPriceChangePercent().compareTo(BigDecimal.ZERO));
  }

  @Test
  void unchangedPrice_returnsZeroPercent() {
    Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    assertEquals(0, stock.getLatestPriceChangePercent().compareTo(BigDecimal.ZERO));
  }
}
