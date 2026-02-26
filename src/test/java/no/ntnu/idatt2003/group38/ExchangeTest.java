package no.ntnu.idatt2003.group38;

import no.ntnu.idatt2003.group38.transaction.Transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;

public class ExchangeTest {

    private Exchange exchange;
    private Player player;
    private Stock appleStock;

    @BeforeEach
    void setUp() {
        appleStock = new Stock("AAPL", "Apple", new BigDecimal("150"));
        exchange = new Exchange("Test Exchange", List.of(appleStock));
        player = new Player("Lionel Messi", new BigDecimal("10000"));
    }

    @Test
    void constructor_setsNameAndStartsAtWeekOne() {
        assertEquals("Test Exchange", exchange.getName());
        assertEquals(1, exchange.getWeek());
    }

    @Test
    void hasStock_existingSymbol_returnsTrue() {
        assertTrue(exchange.hasStock("AAPL"));
    }

    @Test
    void getStock_existingSymbol_returnsCorrectStock() {
        Stock stock = exchange.getStock("AAPL");
        assertSame(appleStock, stock);
    }

    @Test
    void buy_validPurchase_updatesPlayerState() {
        BigDecimal initialMoney = player.getMoney();

        Transaction transaction = exchange.buy("AAPL", new BigDecimal("10"), player);

        assertNotNull(transaction);
        assertEquals(1, player.getPortfolio().getShares().size());
        assertTrue(player.getTransactionArchive().getTransactions().contains(transaction));

        BigDecimal expectedCost = new BigDecimal("1507.500");
        BigDecimal expectedRemaining = initialMoney.subtract(expectedCost);

        assertEquals(0, player.getMoney().compareTo(expectedRemaining));
    }

    @Test
    void sell_validSale_updatesPlayerState() {
        exchange.buy("AAPL", new BigDecimal("10"), player);
        Share ownedShare = player.getPortfolio().getShares().getFirst();

        appleStock.addNewSalesPrice(new BigDecimal("200"));

        BigDecimal initialMoney = player.getMoney();
        Transaction transaction = exchange.sell(ownedShare, player);

        assertNotNull(transaction);
        assertFalse(player.getPortfolio().contains(ownedShare));
        assertTrue(player.getTransactionArchive().getTransactions().contains(transaction));

        BigDecimal expectedProceeds = new BigDecimal("1836");
        BigDecimal expectedMoney = initialMoney.add(expectedProceeds);

        assertEquals(0, player.getMoney().compareTo(expectedMoney));
    }

    @Test
    void advance_incrementsWeek() {
        assertEquals(1, exchange.getWeek());
        exchange.advance();
        assertEquals(2, exchange.getWeek());
    }

    //NEGATIVE TESTS

    @Test
    void constructor_nullName_throwsException() {
        assertThrows(NullPointerException.class, () -> new Exchange(null, List.of(appleStock)));
    }

    @Test
    void constructor_nullStocks_throwsException() {
        assertThrows(NullPointerException.class, () -> new Exchange("X", null));
    }

    @Test
    void buy_invalidInputs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.buy(null, new BigDecimal("1"), player));
        assertThrows(IllegalArgumentException.class, () -> exchange.buy("AAPL", null, player));
        assertThrows(IllegalArgumentException.class, () -> exchange.buy("AAPL", BigDecimal.ZERO, player));
        assertThrows(IllegalArgumentException.class, () -> exchange.buy("AAPL", new BigDecimal("-1"), player));
        assertThrows(IllegalArgumentException.class, () -> exchange.buy("AAPL", new BigDecimal("1"), null));
    }

    @Test
    void sell_invalidInputs_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.sell(null, player));

        Share notOwned = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));
        assertThrows(IllegalArgumentException.class, () -> exchange.sell(notOwned, player));

        exchange.buy("AAPL", new BigDecimal("10"), player);
        Share ownedShare = player.getPortfolio().getShares().getFirst();
        assertThrows(IllegalArgumentException.class, () -> exchange.sell(ownedShare, null));
    }
}