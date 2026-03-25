package no.ntnu.idatt2003.group38.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PortfolioTest {

  private Portfolio portfolio;
  private Share appleShare;
  private Share googleShare;

  private Stock createAppleStock() {
    return new Stock("AAPL", "Apple", new BigDecimal("150"));
  }

  private Stock createGoogleStock() {
    return new Stock("GOOG", "Google", new BigDecimal("2800"));
  }

  @BeforeEach
  void setup() {
    portfolio = new Portfolio();
    appleShare = new Share(createAppleStock(), new BigDecimal("10"), new BigDecimal("150"));
    googleShare = new Share(createGoogleStock(), new BigDecimal("5"), new BigDecimal("2800"));
  }

  // POSITIVE TESTS

  @Test
  void addShare_validShare_addsSuccessfully() {
    assertTrue(portfolio.addShare(appleShare));
    assertTrue(portfolio.contains(appleShare));
  }

  @Test
  void removeShare_existingShare_removesSuccessfully() {
    portfolio.addShare(appleShare);
    assertTrue(portfolio.removeShare(appleShare));
    assertFalse(portfolio.contains(appleShare));
  }

  @Test
  void getSharesBySymbol_returnsCorrectShares() {
    portfolio.addShare(appleShare);
    portfolio.addShare(googleShare);

    List<Share> appleShares = portfolio.getShares("AAPL");
    assertEquals(1, appleShares.size());
    assertEquals("AAPL", appleShares.getFirst().getStock().getSymbol());
  }

  @Test
  void getNetWorth_emptyPortfolio_returnsZero() {
    assertEquals(BigDecimal.ZERO, portfolio.getNetWorth());
  }

  @Test
  void getNetWorth_withShares_returnsTotalSaleValue() {
    Stock appleStock = createAppleStock();
    Stock googleStock = createGoogleStock();
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    googleStock.addNewSalesPrice(new BigDecimal("3000"));

    Share updatedAppleShare = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));
    Share updatedGoogleShare = new Share(googleStock, new BigDecimal("5"), new BigDecimal("2800"));

    portfolio.addShare(updatedAppleShare);
    portfolio.addShare(updatedGoogleShare);

    assertEquals(new BigDecimal("16431.0000"), portfolio.getNetWorth());
  }

  // NEGATIVE TESTS

  @Test
  void addShare_nullShare_throwsException() {
    assertThrows(NullPointerException.class, () -> portfolio.addShare(null));
  }

  @Test
  void removeShare_nullShare_throwsException() {
    assertThrows(NullPointerException.class, () -> portfolio.removeShare(null));
  }

  @Test
  void contains_nullShare_throwsException() {
    assertThrows(NullPointerException.class, () -> portfolio.contains(null));
  }

  @Test
  void getShares_nullSymbol_throwsException() {
    assertThrows(NullPointerException.class, () -> portfolio.getShares(null));
  }

  @Test
  void getShares_emptySymbol_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> portfolio.getShares(""));
  }
}
