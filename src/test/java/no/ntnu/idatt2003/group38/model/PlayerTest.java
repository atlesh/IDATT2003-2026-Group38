package no.ntnu.idatt2003.group38.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Player}.
 */
public class PlayerTest {

  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("Alice", new BigDecimal("5000"));
  }

  // POSITIVE TESTS

  @Test
  void constructor_initialisesFieldsCorrectly() {
    assertEquals("Alice", player.getName());
    assertEquals(new BigDecimal("5000"), player.getMoney());
    assertNotNull(player.getPortfolio());
    assertNotNull(player.getTransactionArchive());
  }

  @Test
  void addMoney_increasesBalance() {
    player.addMoney(new BigDecimal("250"));
    assertEquals(new BigDecimal("5250"), player.getMoney());
  }

  @Test
  void withdrawMoney_decreasesBalance() {
    player.withdrawMoney(new BigDecimal("1000"));
    assertEquals(new BigDecimal("4000"), player.getMoney());
  }

  @Test
  void getNetWorth_withoutShares_returnsCurrentMoney() {
    player.withdrawMoney(new BigDecimal("1000"));

    assertEquals(new BigDecimal("4000"), player.getNetWorth());
  }

  @Test
  void getNetWorth_withPortfolio_returnsMoneyPlusPortfolioValue() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("150"));
    stock.addNewSalesPrice(new BigDecimal("200"));
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

    player.getPortfolio().addShare(share);

    assertEquals(new BigDecimal("6836.0000"), player.getNetWorth());
  }

  // NEGATIVE TESTS
  @Test
  void nullName_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Player(null, new BigDecimal("1000")));
  }

  @Test
  void nullStartingMoney_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Player("Bob", null));
  }

  @Test
  void addMoney_nullAmount_throwsException() {
    assertThrows(NullPointerException.class,
        () -> player.addMoney(null));
  }

  @Test
  void addMoney_negativeOrZeroAmount_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> player.addMoney(BigDecimal.ZERO));
    assertThrows(IllegalArgumentException.class,
        () -> player.addMoney(new BigDecimal("-5")));
  }

  @Test
  void withdrawMoney_nullAmount_throwsException() {
    assertThrows(NullPointerException.class,
        () -> player.withdrawMoney(null));
  }

  @Test
  void withdrawMoney_negativeOrZeroAmount_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> player.withdrawMoney(BigDecimal.ZERO));
    assertThrows(IllegalArgumentException.class,
        () -> player.withdrawMoney(new BigDecimal("-10")));
  }


  // getPlayerStatus

  @Test
  void returnsNovice() {
    assertEquals("Novice", player.getPlayerStatus());
  }

  @Test
  void returnsInvestor() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
    Exchange exchange = new Exchange("Test Exchange", List.of(stock));

    for (int i = 0; i < 10; i++) {
      exchange.buy("AAPL", new BigDecimal("1"), player);
      exchange.advance();
    }

    stock.addNewSalesPrice(new BigDecimal("10000.00"));

    assertEquals("Investor", player.getPlayerStatus());
  }

  @Test
  void returnsSpeculator() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
    Exchange exchange = new Exchange("Test Exchange", List.of(stock));

    for (int i = 0; i < 20; i++) {
      exchange.buy("AAPL", new BigDecimal("1"), player);
      exchange.advance();
    }

    stock.addNewSalesPrice(new BigDecimal("100000.00"));

    assertEquals("Speculator", player.getPlayerStatus());
  }

  @Test
  void enoughWeeksButInsufficientGain_returnsNovice() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
    Exchange exchange = new Exchange("Test Exchange", List.of(stock));

    for (int i = 0; i < 10; i++) {
      exchange.buy("AAPL", new BigDecimal("1"), player);
      exchange.advance();
    }

    assertEquals("Novice", player.getPlayerStatus());
  }

  @Test
  void sufficientGainButNotEnoughWeeks_returnsNovice() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
    Exchange exchange = new Exchange("Test Exchange", List.of(stock));

    exchange.buy("AAPL", new BigDecimal("1"), player);
    stock.addNewSalesPrice(new BigDecimal("100000.00"));

    assertEquals("Novice", player.getPlayerStatus());
  }

  @Test
  void meetsInvestorButNotSpeculatorWeeks_returnsInvestor() {
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("100.00"));
    Exchange exchange = new Exchange("Test Exchange", List.of(stock));

    for (int i = 0; i < 10; i++) {
      exchange.buy("AAPL", new BigDecimal("1"), player);
      exchange.advance();
    }

    stock.addNewSalesPrice(new BigDecimal("100000.00"));

    assertEquals("Investor", player.getPlayerStatus());
  }

  // getNetWorthHistory

  @Test
  void newPlayer_containsStartingMoney() {
    List<BigDecimal> history = player.getNetWorthHistory();

    assertEquals(1, history.size());
    assertEquals(0, history.getFirst().compareTo(new BigDecimal("5000")));
  }

  @Test
  void returnsImmutableCopy() {
    List<BigDecimal> history = player.getNetWorthHistory();

    assertThrows(UnsupportedOperationException.class,
        () -> history.add(new BigDecimal("9999")));
  }

  @Test
  void returnsSnapshot_notLiveView() {
    List<BigDecimal> historyBefore = player.getNetWorthHistory();
    assertEquals(1, historyBefore.size());

    player.recordNetWorthSnapshot();

    assertEquals(1, historyBefore.size());
    assertEquals(2, player.getNetWorthHistory().size());
  }

  // recordNetWorthSnapshot

  @Test
  void appendsCurrentNetWorthToHistory() {
    player.addMoney(new BigDecimal("1000"));

    player.recordNetWorthSnapshot();
    List<BigDecimal> history = player.getNetWorthHistory();

    assertEquals(2, history.size());
    assertEquals(0, history.get(0).compareTo(new BigDecimal("5000")));
    assertEquals(0, history.get(1).compareTo(new BigDecimal("6000")));
  }

  @Test
  void multipleCalls_preservesChronologicalOrder() {
    player.addMoney(new BigDecimal("1000"));
    player.recordNetWorthSnapshot(); // 6000

    player.withdrawMoney(new BigDecimal("2000"));
    player.recordNetWorthSnapshot(); // 4000

    player.addMoney(new BigDecimal("500"));
    player.recordNetWorthSnapshot(); // 4500

    List<BigDecimal> history = player.getNetWorthHistory();

    assertEquals(4, history.size());
    assertEquals(0, history.get(0).compareTo(new BigDecimal("5000")));
    assertEquals(0, history.get(1).compareTo(new BigDecimal("6000")));
    assertEquals(0, history.get(2).compareTo(new BigDecimal("4000")));
    assertEquals(0, history.get(3).compareTo(new BigDecimal("4500")));
  }

  @Test
  void includesPortfolioValue() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("150"));
    stock.addNewSalesPrice(new BigDecimal("200"));
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));
    player.getPortfolio().addShare(share);

    player.recordNetWorthSnapshot();
    List<BigDecimal> history = player.getNetWorthHistory();

    assertEquals(2, history.size());
    assertEquals(0, history.get(1).compareTo(player.getNetWorth()));
  }
}
