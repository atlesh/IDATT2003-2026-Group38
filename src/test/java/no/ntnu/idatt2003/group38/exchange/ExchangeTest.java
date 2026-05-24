package no.ntnu.idatt2003.group38.exchange;

import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.transaction.Transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Unit tests for {@link Exchange}.
 */
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

    @Test
    void getGainersReturnsStocksWithLargestPositiveWeeklyChange() {
        Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));
        Stock teslaStock = new Stock("TSLA", "Tesla", new BigDecimal("200.00"));
        Stock netflixStock = new Stock("NFLX", "Netflix", new BigDecimal("400.00"));

        appleStock.addNewSalesPrice(new BigDecimal("155.00"));
        microsoftStock.addNewSalesPrice(new BigDecimal("315.00"));
        teslaStock.addNewSalesPrice(new BigDecimal("190.00"));
        netflixStock.addNewSalesPrice(new BigDecimal("405.00"));

        Exchange statisticsExchange = new Exchange(
                "Statistics Exchange",
                List.of(appleStock, microsoftStock, teslaStock, netflixStock)
        );

        List<Stock> gainers = statisticsExchange.getGainers(2);

        assertEquals(List.of(microsoftStock, appleStock), gainers);
    }

    @Test
    void getLosersReturnsStocksWithLargestNegativeWeeklyChange() {
        Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));
        Stock teslaStock = new Stock("TSLA", "Tesla", new BigDecimal("200.00"));
        Stock netflixStock = new Stock("NFLX", "Netflix", new BigDecimal("400.00"));

        appleStock.addNewSalesPrice(new BigDecimal("155.00"));
        microsoftStock.addNewSalesPrice(new BigDecimal("280.00"));
        teslaStock.addNewSalesPrice(new BigDecimal("190.00"));
        netflixStock.addNewSalesPrice(new BigDecimal("395.00"));

        Exchange statisticsExchange = new Exchange(
                "Statistics Exchange",
                List.of(appleStock, microsoftStock, teslaStock, netflixStock)
        );

        List<Stock> losers = statisticsExchange.getLosers(2);

        assertEquals(List.of(microsoftStock, teslaStock), losers);
    }

    @Test
    void getGainers_withoutPriceHistoryReturnsEmptyList() {
        assertTrue(exchange.getGainers(5).isEmpty());
    }

    @Test
    void getLosers_withoutPriceHistoryReturnsEmptyList() {
        assertTrue(exchange.getLosers(5).isEmpty());
    }

    @Test
    void hasStock_trimmedLowerCaseSymbol_returnsTrue() {
        assertTrue(exchange.hasStock("  aapl  "));
    }

    @Test
    void getStock_trimmedLowerCaseSymbol_returnsCorrectStock() {
        assertSame(appleStock, exchange.getStock("  aapl"));
    }

    @Test
    void findStocks_matchesCompanyName_caseInsensitive() {
        Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));
        Exchange searchExchange = new Exchange("Search Exchange", List.of(appleStock, microsoftStock));

        assertEquals(List.of(microsoftStock), searchExchange.findStocks("soft"));
    }

    @Test
    void findStocks_blankSearchTerm_ReturnsEmptyList() {
        assertEquals(List.of(appleStock), exchange.findStocks("  "));
    }

    @Test
    void getGainers_limitZero_returnsEmptyList() {
        appleStock.addNewSalesPrice(new BigDecimal("155.00"));

        assertTrue(exchange.getGainers(0).isEmpty());
    }

    @Test
    void getGainers_equalChange_sortsBySymbol() {
        Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));
        Stock amazonStock = new Stock("AMZN", "Amazon", new BigDecimal("100.00"));

        microsoftStock.addNewSalesPrice(new BigDecimal("310.00"));
        amazonStock.addNewSalesPrice(new BigDecimal("110.00"));

        Exchange statisticsExchange = new Exchange(
                "Statistics Exchange",
                List.of(microsoftStock, amazonStock)
        );

        assertEquals(List.of(amazonStock, microsoftStock), statisticsExchange.getGainers(5));
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
    void hasStock_nullSymbol_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.hasStock(null));
    }

    @Test
    void getStock_unknownSymbol_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.getStock("MSFT"));
    }

    @Test
    void findStocks_nullSearchTerm_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.findStocks(null));
    }

    @Test
    void buy_unknownSymbol_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.buy("MSFT", BigDecimal.ONE, player));
    }

    @Test
    void buy_insufficientFunds_throwsException() {
        assertThrows(IllegalStateException.class, () -> exchange.buy("AAPL", new BigDecimal("1000.00"), player));
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

    @Test
    void getGainers_negativeLimitThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.getGainers(-1));
    }

    @Test
    void getLosers_negativeLimitThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> exchange.getLosers(-1));
    }


  // 3-arg sell(Share, BigDecimal, Player) - happy paths

  @Test
  void sellWithQuantity_partialSale_reducesOwnedQuantity() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share ownedShare = player.getPortfolio().getShares().getFirst();

    appleStock.addNewSalesPrice(new BigDecimal("200"));

    Transaction transaction = exchange.sell(ownedShare, new BigDecimal("4"), player);

    assertNotNull(transaction);
    // The original lot is removed and a remainder of 6 is added back
    assertEquals(1, player.getPortfolio().getShares().size());
    assertEquals(0, player.getPortfolio().getShares().getFirst()
        .getQuantity().compareTo(new BigDecimal("6")));
    assertTrue(player.getTransactionArchive().getTransactions().contains(transaction));
  }

  @Test
  void sellWithQuantity_fullQuantity_delegatesToFullSale() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share ownedShare = player.getPortfolio().getShares().getFirst();

    appleStock.addNewSalesPrice(new BigDecimal("200"));

    Transaction transaction = exchange.sell(ownedShare, new BigDecimal("10"), player);

    assertNotNull(transaction);
    assertFalse(player.getPortfolio().contains(ownedShare));
    assertTrue(player.getPortfolio().getShares().isEmpty());
  }

// sell with 3 arguments

  @Test
  void nullShare_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(null, new BigDecimal("1"), player));
  }

  @Test
  void nullQuantity_throwsException() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share ownedShare = player.getPortfolio().getShares().getFirst();

    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(ownedShare, null, player));
  }

  @Test
  void zeroOrNegativeQuantity_throwsException() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share ownedShare = player.getPortfolio().getShares().getFirst();

    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(ownedShare, BigDecimal.ZERO, player));
    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(ownedShare, new BigDecimal("-1"), player));
  }

  @Test
  void nullPlayer_throwsException() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share ownedShare = player.getPortfolio().getShares().getFirst();

    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(ownedShare, new BigDecimal("1"), null));
  }

  @Test
  void shareNotOwned_throwsException() {
    Share notOwned = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150"));

    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(notOwned, new BigDecimal("5"), player));
  }

  @Test
  void quantityExceedsOwned_throwsException() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share ownedShare = player.getPortfolio().getShares().getFirst();

    assertThrows(IllegalArgumentException.class,
        () -> exchange.sell(ownedShare, new BigDecimal("11"), player));
  }

  @Test
    void sellAll_multipleOwnedShares_clearsPortfolioAndReturnsCommittedSales() {
        Stock microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300"));
        Exchange liquidationExchange = new Exchange("Liquidation Exchange", List.of(appleStock, microsoftStock));

        liquidationExchange.buy("AAPL", new BigDecimal("10"), player);
        liquidationExchange.buy("MSFT", new BigDecimal("5"), player);

        List<Transaction> sales = liquidationExchange.sellAll(player);

        assertEquals(2, sales.size());
        assertTrue(player.getPortfolio().getShares().isEmpty());
        assertTrue(sales.stream().allMatch(Transaction::isCommitted));
        assertEquals(4, player.getTransactionArchive().getTransactions().size());
    }

    @Test
    void sellAll_emptyPortfolio_returnsEmptyList() {
        List<Transaction> sales = exchange.sellAll(player);

        assertTrue(sales.isEmpty());
        assertTrue(player.getPortfolio().getShares().isEmpty());
        assertTrue(player.getTransactionArchive().getTransactions().isEmpty());
    }

    @Test
    void sellAll_nullPlayer_throwsException() {
        assertThrows(IllegalArgumentException.class,  () -> exchange.sell(null, player));
    }
}
