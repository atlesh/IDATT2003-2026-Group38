package no.ntnu.idatt2003.group38.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import no.ntnu.idatt2003.group38.Stock;
import no.ntnu.idatt2003.group38.Share;
import no.ntnu.idatt2003.group38.Player;

public class SaleTest {
  private Share appleShare;
  private Player player;
  private Stock appleStock;

  @BeforeEach
  void setup() {
    appleStock = new Stock("AAPL", "Apple", new BigDecimal("150"));
    appleShare = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));
    player = new Player("John Doe", new BigDecimal("5000"));

    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(player);
  }

  // POSITIVE TESTS

  @Test
  void validSale_removeShareFromPortfolio() {
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    Sale sale = new Sale(appleShare, 2);

    sale.commit(player);

    assertFalse(player.getPortfolio().contains(appleShare));
  }

  @Test
  void addsCorrectAmountToMoney() {
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    BigDecimal initialMoney = player.getMoney();

    Sale sale = new Sale(appleShare, 2);
    sale.commit(player);

    // Gross: 200 * 10 = 2000
    // Commission: 2000 * 0.01 = 20
    // Profit: 2000 - 20 - 1500 = 480
    // Tax: 480 * 0.30 = 144
    // Net proceeds: 2000 - 20 - 144 = 1836
    BigDecimal expectedProceeds = new BigDecimal("1836");
    BigDecimal expectedMoney = initialMoney.add(expectedProceeds);

    assertEquals(0, player.getMoney().compareTo(expectedMoney));
  }

  @Test
  void addToTransactionArchive() {
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    Sale sale = new Sale(appleShare, 2);
    sale.commit(player);

    assertEquals(2, player.getTransactionArchive().getTransactions().size());
    assertTrue(player.getTransactionArchive().getTransactions().contains(sale));
  }

  @Test
  void markAsCommitted() {
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    Sale sale = new Sale(appleShare, 2);

    assertFalse(sale.isCommitted());
    sale.commit(player);
    assertTrue(sale.isCommitted());
  }

  // NEGATIVE TESTS

  @Test
  void nullPlayer_throwsException() {
    Sale sale = new Sale(appleShare, 2);
    assertThrows(NullPointerException.class, () -> sale.commit(null));
  }

  @Test
  void playerDoesNotOwnShare_throwsException() {
    Player otherPlayer = new Player("Other", new BigDecimal("5000"));
    Sale sale = new Sale(appleShare, 2);

    assertThrows(IllegalStateException.class, () -> sale.commit(otherPlayer));
  }

  @Test
  void alreadyCommitted_throwsException() {
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    Sale sale = new Sale(appleShare, 2);
    sale.commit(player);

    assertThrows(IllegalStateException.class, () -> sale.commit(player));
  }

  @Test
  void alreadyCommitted_doesNotDoubleSell() {
    appleStock.addNewSalesPrice(new BigDecimal("200"));
    Sale sale = new Sale(appleShare, 2);
    sale.commit(player);

    assertThrows(IllegalStateException.class, () -> sale.commit(player));
    assertFalse(player.getPortfolio().contains(appleShare));
    assertEquals(2, player.getTransactionArchive().getTransactions().size());
  }
}
