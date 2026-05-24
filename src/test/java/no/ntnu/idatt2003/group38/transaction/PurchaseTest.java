package no.ntnu.idatt2003.group38.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Unit tests for {@link Purchase}.
 */
public class PurchaseTest {
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
  void sufficientFunds_purchaseSuccessful() {
    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(player);

    assertTrue(purchase.isCommitted());
    assertTrue(player.getPortfolio().contains(appleShare));
    assertTrue(player.getTransactionArchive().getTransactions().contains(purchase));
  }

  @Test
  void sufficientFunds_deductsCorrectAmount() {
    Purchase purchase = new Purchase(appleShare, 1);
    BigDecimal initialMoney = player.getMoney();

    purchase.commit(player);

    BigDecimal expectedCost = new BigDecimal("1507.500"); // 1500 + 7.5
    BigDecimal expectedRemaining = initialMoney.subtract(expectedCost);
    assertEquals(expectedRemaining, player.getMoney());
  }

  @Test
  void exactFunds_purchaseSuccessful() {
    // total cost for the purchase is 1507.500 (gross 1500 + 0.5% commission) @40
    BigDecimal exactCost = new BigDecimal("1507.500");
    Player playerWithExactFunds = new Player("Jane", exactCost);

    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(playerWithExactFunds);

    assertTrue(purchase.isCommitted());

    assertEquals(0, playerWithExactFunds.getMoney().compareTo(BigDecimal.ZERO));
  }


  @Test
  void addedToTransactionArchive() {
    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(player);

    assertEquals(1, player.getTransactionArchive().getTransactions().size());
    assertEquals(purchase, player.getTransactionArchive().getTransactions().getFirst());
  }

  // NEGATIVE TESTS

  @Test
  void nullPlayer_throwsException() {
    Purchase purchase = new Purchase(appleShare, 1);
    assertThrows(NullPointerException.class, () -> purchase.commit(null));
  }

  @Test
  void insufficientFunds_throwsException() {
    Player poorPlayer = new Player("Poor", new BigDecimal("100"));
    Purchase purchase = new Purchase(appleShare, 1);

    assertThrows(IllegalStateException.class, () -> purchase.commit(poorPlayer));
  }

  @Test
  void alreadyCommitted_throwsException() {
    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(player);

    assertThrows(IllegalStateException.class, () -> purchase.commit(player));
  }

  @Test
  void alreadyCommitted_doesNotDoublePurchase() {
    Purchase purchase = new Purchase(appleShare, 1);
    purchase.commit(player);

    assertThrows(IllegalStateException.class, () -> purchase.commit(player));
    assertEquals(1, player.getPortfolio().getShares().size());
  }
}
