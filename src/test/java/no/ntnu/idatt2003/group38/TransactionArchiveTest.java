package no.ntnu.idatt2003.group38;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.transaction.Sale;


public class TransactionArchiveTest {
  private TransactionArchive archive;
  private Share appleShare;
  private Share googleShare;

  @BeforeEach
  void setup() {
    archive = new TransactionArchive();

    Stock appleStock = new Stock("AAPL", "Apple", new BigDecimal("150"));
    Stock googleStock = new Stock("GOOG", "Google", new BigDecimal("2800"));

    appleShare = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));
    googleShare = new Share(googleStock, new BigDecimal("5"), new BigDecimal("2800"));

    Player player = new Player("John Doe", new BigDecimal("50000"));
  }

  // POSITIVE TESTS

  @Test
  void constructor_createsEmptyArchive() {
    assertTrue(archive.isEmpty());
    assertEquals(0, archive.getTransactions().size());
  }

  @Test
  void validTransaction_addsSuccessfully() {
    Purchase purchase = new Purchase(appleShare, 1);
    assertTrue(archive.add(purchase));
    assertFalse(archive.isEmpty());
    assertEquals(1, archive.getTransactions().size());
    assertTrue(archive.getTransactions().contains(purchase));
  }

  @Test
  void multipleTransactions_allAdded() {
    Purchase purchase = new Purchase(appleShare, 1);
    Sale sale = new Sale(appleShare, 2);

    assertTrue(archive.add(purchase));
    assertTrue(archive.add(sale));

    assertEquals(2, archive.getTransactions().size());
    assertTrue(archive.getTransactions().contains(purchase));
    assertTrue(archive.getTransactions().contains(sale));
  }

  @Test
  void isEmpty_withNoTransactions_returnsTrue() {
    assertTrue(archive.isEmpty());
  }

  @Test
  void isEmpty_withTransactions_returnsFalse() {
    archive.add(new Purchase(appleShare, 1));
    assertFalse(archive.isEmpty());
  }

  @Test
  void returnsAllTransactions() {
    Purchase purchase1 = new Purchase(appleShare, 1);
    Purchase purchase2 = new Purchase(googleShare, 1);
    Sale sale = new Sale(appleShare, 2);

    archive.add(purchase1);
    archive.add(purchase2);
    archive.add(sale);

    List<Transaction> transactions = archive.getTransactions();
    assertEquals(3, transactions.size());
    assertTrue(transactions.contains(purchase1));
    assertTrue(transactions.contains(purchase2));
    assertTrue(transactions.contains(sale));
  }

  @Test
  void specificWeek_returnsPurchasesFromThatWeek() {
    Purchase purchase1 = new Purchase(appleShare, 1);
    Purchase purchase2 = new Purchase(googleShare, 1);
    Sale sale = new Sale(appleShare, 2);

    archive.add(purchase1);
    archive.add(purchase2);
    archive.add(sale);

    List<Purchase> week1Purchases = archive.getPurchases(1);
    assertEquals(2, week1Purchases.size());
    assertTrue(week1Purchases.contains(purchase1));
    assertTrue(week1Purchases.contains(purchase2));
  }

  @Test
  void weekWithNoPurchases_returnsEmptyList() {
    archive.add(new Sale(appleShare, 1));

    List<Purchase> purchases = archive.getPurchases(1);
    assertTrue(purchases.isEmpty());
  }

  @Test
  void nonexistentWeek_returnsEmptyList() {
    archive.add(new Purchase(appleShare, 1));

    List<Purchase> purchases = archive.getPurchases(999);
    assertTrue(purchases.isEmpty());
  }

  @Test
  void specificWeek_returnsSalesFromThatWeek() {
    Purchase purchase = new Purchase(appleShare, 1);
    Sale sale1 = new Sale(appleShare, 2);
    Sale sale2 = new Sale(googleShare, 2);

    archive.add(purchase);
    archive.add(sale1);
    archive.add(sale2);

    List<Sale> week2Sales = archive.getSales(2);
    assertEquals(2, week2Sales.size());
    assertTrue(week2Sales.contains(sale1));
    assertTrue(week2Sales.contains(sale2));
  }

  @Test
  void weekWithNoSales_returnsEmptyList() {
    archive.add(new Purchase(appleShare, 1));

    List<Sale> sales = archive.getSales(1);
    assertTrue(sales.isEmpty());
  }

  @Test
  void nonexistentSalesWeek_returnsEmptyList() {
    archive.add(new Sale(appleShare, 1));

    List<Sale> sales = archive.getSales(999);
    assertTrue(sales.isEmpty());
  }

  @Test
  void countDistinctWeeks_noTransactions_returnsZero() {
    assertEquals(0, archive.countDistinctWeeks());
  }

  @Test
  void singleWeek_returnsOne() {
    archive.add(new Purchase(appleShare, 1));
    archive.add(new Purchase(googleShare, 1));

    assertEquals(1, archive.countDistinctWeeks());
  }

  @Test
  void multipleWeeks_returnsCorrectCount() {
    archive.add(new Purchase(appleShare, 1));
    archive.add(new Purchase(googleShare, 2));
    archive.add(new Sale(appleShare, 3));
    archive.add(new Sale(googleShare, 3));

    assertEquals(3, archive.countDistinctWeeks());
  }

  @Test
  void duplicateWeeks_countsOnce() {
    archive.add(new Purchase(appleShare, 1));
    archive.add(new Purchase(googleShare, 1));
    archive.add(new Sale(appleShare, 1));

    assertEquals(1, archive.countDistinctWeeks());
  }

  // NEGATIVE TESTS

  @Test
  void nullTransaction_throwsException() {
    assertThrows(NullPointerException.class, () -> archive.add(null));
  }

  @Test
  void getPurchases_weekLessThanOne_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> archive.getPurchases(0));
    assertThrows(IllegalArgumentException.class, () -> archive.getPurchases(-1));
    assertThrows(IllegalArgumentException.class, () -> archive.getPurchases(-100));
  }

  @Test
  void getSales_weekLessThanOne_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> archive.getSales(0));
    assertThrows(IllegalArgumentException.class, () -> archive.getSales(-1));
    assertThrows(IllegalArgumentException.class, () -> archive.getSales(-100));
  }
}
