package no.ntnu.idatt2003.group38.exchange;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import no.ntnu.idatt2003.group38.event.EventGenerator;
import no.ntnu.idatt2003.group38.event.EventNotice;
import no.ntnu.idatt2003.group38.event.MarketEvent;
import no.ntnu.idatt2003.group38.event.Rumor;
import no.ntnu.idatt2003.group38.event.RumorGenerator;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.observer.Observable;
import no.ntnu.idatt2003.group38.transaction.Sale;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.transaction.TransactionFactory;

/**
 * Represents a stock exchange where stocks are listed and can be traded by players.
 *
 * <p>The exchange maintains a collection of stocks and
 * tracks the current trading week</p>
 *
 * <p>Players can buy and sell shares through the exchange,
 * and the market can advance to simulate price changes</p>
 */
public class Exchange extends Observable {

  private final String name;
  private int week;
  private final Map<String, Stock> stockMap;
  private final Random random;
  private final EventGenerator eventGenerator;
  private final RumorGenerator rumorGenerator;
  private final List<EventNotice> lastWeekEvents = new ArrayList<>();
  private List<Rumor> activeRumors = List.of();

  /**
   * Creates a new stock exchange with the given name and listed stocks.
   *
   * @param name   the name of the exchange; must not be {@code null}
   * @param stocks the list of stocks listed in the exchange; must not be {@code null}
   * @throws NullPointerException if {@code name} or {@code stocks} is {@code null}
   */
  public Exchange(String name, List<Stock> stocks) {
    Objects.requireNonNull(stocks, "Stocks cannot be null");
    this.name = Objects.requireNonNull(name, "Name cannot be null");
    this.week = 1;
    this.random = new Random();
    this.eventGenerator = new EventGenerator(this.random);
    this.rumorGenerator = new RumorGenerator(this.random);
    this.stockMap = new HashMap<>();

    for (Stock stock : stocks) {
      stockMap.put(stock.getSymbol().toUpperCase(), stock);
    }
  }

  /**
   * Returns the name of the exchange.
   *
   * @return the exchange name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the current trading week.
   *
   * @return the current week number
   */
  public int getWeek() {
    return this.week;
  }

  /**
   * Checks whether a stock with the given symbol is listed on the exchange.
   *
   * @param symbol the stock symbol; must not be {@code null}
   * @return {@code true} if the stock exists, {@code false} otherwise
   * @throws IllegalArgumentException if {@code symbol} is {@code null}
   */
  public boolean hasStock(String symbol) {
    if (symbol == null) {
      throw new IllegalArgumentException("Symbol cannot be null");
    }

    String normalized = symbol.trim().toUpperCase();

    if (normalized.isEmpty()) {
      return false;
    }

    return this.stockMap.containsKey(normalized);
  }

  /**
   * Returns the stock with the given symbol.
   *
   * @param symbol the stock symbol. Must not be {@code null} or empty
   * @return the corresponding {@link Stock}
   * @throws IllegalArgumentException if the symbol is {@code null}, empty,
   *                                  or not listed in the exchange
   */
  public Stock getStock(String symbol) {
    if (symbol == null) {
      throw new IllegalArgumentException("Symbol cannot be null");
    }

    String normalized = symbol.trim().toUpperCase();

    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Symbol cannot be empty");
    }

    Stock stock = stockMap.get(normalized);

    if (stock == null) {
      throw new IllegalArgumentException(
          "Stock with symbol '" + normalized + "' does not exist on this exchange");
    }

    return stock;
  }

  /**
   * Searches for stocks whose symbol or company name contains the given search term.
   * An empty or blank search term returns every stock on the exchange.
   *
   * @param searchTerm the term to search for. Must not be {@code null}
   * @return a list of matching stocks, never {@code null}
   * @throws IllegalArgumentException if {@code searchTerm} is {@code null}
   */
  public List<Stock> findStocks(String searchTerm) {
    if (searchTerm == null) {
      throw new IllegalArgumentException("Search term cannot be null");
    }

    String normalized = searchTerm.trim().toUpperCase();

    if (normalized.isEmpty()) {
      return new ArrayList<>(this.stockMap.values());   // ← return all stocks
    }

    List<Stock> result = new ArrayList<>();

    for (Stock stock : this.stockMap.values()) {
      String symbol = stock.getSymbol().toUpperCase();
      String company = stock.getCompany().toUpperCase();

      if (symbol.contains(normalized) || company.contains(normalized)) {
        result.add(stock);
      }
    }
    return result;
  }

  /**
   * Buys a specified quantity of a stock for the given player.
   *
   * @param symbol   the stock symbol; must not be {@code null}
   * @param quantity the quantity to buy; must be greater than zero
   * @param player   the player performing the purchase; must not be {@code null}
   * @return the committed {@link Transaction}
   * @throws IllegalArgumentException if any requirement is invalid
   */
  public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    if (symbol == null) {
      throw new IllegalArgumentException("Symbol cannot be null");
    }
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }

    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());

    Transaction purchase = TransactionFactory.create(
        TransactionFactory.Type.Purchase, share, this.week);
    purchase.commit(player);
    notifyObservers();
    return purchase;
  }

  /**
   * Sells the given share on behalf of the specified player.
   *
   * @param share  the share to be sold; must not be {@code null}
   * @param player the player performing the sale; must not be {@code null}
   * @return the committed {@link Transaction}
   * @throws IllegalArgumentException if the share is invalid or not owned by the player
   */
  public Transaction sell(Share share, Player player) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }
    if (share.getStock() == null) {
      throw new IllegalArgumentException("Share must reference a stock");
    }
    if (share.getQuantity() == null || share.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Share quantity must be greater than 0");
    }
    if (!player.getPortfolio().contains(share)) {
      throw new IllegalArgumentException("Player does not own this share");
    }

    Transaction sale = TransactionFactory.create(
        TransactionFactory.Type.Sale, share, this.week);
    sale.commit(player);
    notifyObservers();
    return sale;
  }

  /**
   * Sells part of an owned share lot.
   *
   * <p>If {@code quantity} equals the full lot quantity,
   * this delegates to {@link #sell(Share, Player)}.
   * Otherwise it creates and commits a partial {@link Sale}
   * while leaving the remaining quantity in the original lot.</p>
   *
   * @param share    the owned share lot to sell; must not be {@code null}
   * @param quantity the quantity to sell; must be greater than {@code 0}
   *                 and no greater than the owned quantity
   * @param player   the player who owns the share lot; must not be {@code null}
   * @return the committed sale transaction
   * @throws IllegalArgumentException if any argument is invalid,
   *                                  if the share does not reference a stock,
   *                                  if the player does not own the share,
   *                                  or if {@code quantity} exceeds the owned quantity
   */
  public Transaction sell(Share share, BigDecimal quantity, Player player) {
    if (share == null) {
      throw new IllegalArgumentException("Share cannot be null");
    }
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }
    if (share.getStock() == null) {
      throw new IllegalArgumentException("Share must reference a stock");
    }
    if (!player.getPortfolio().contains(share)) {
      throw new IllegalArgumentException("Player does not own this share");
    }
    if (quantity.compareTo(share.getQuantity()) > 0) {
      throw new IllegalArgumentException("Cannot sell more than owned quantity");
    }

    if (quantity.compareTo(share.getQuantity()) == 0) {
      return sell(share, player);
    }

    Share soldPart = new Share(share.getStock(), quantity, share.getPurchasePrice());
    Transaction sale = new Sale(soldPart, share, this.week);
    sale.commit(player);
    notifyObservers();
    return sale;
  }

  /**
   * Sells every named share in the player's portfolio.
   *
   * @param player the player whose holdings should be liquidated
   * @return the committed sale transactions
   * @throws IllegalArgumentException if {@code player} is {@code null}
   */
  public List<Transaction> sellAll(Player player) {
    if (player == null) {
      throw new IllegalArgumentException("Player cannot be null");
    }
    List<Share> ownedShares = new ArrayList<>(player.getPortfolio().getShares());
    List<Transaction> transactions = new ArrayList<>();

    for (Share share : ownedShares) {
      transactions.add(sell(share, share.getQuantity(), player));
    }

    return transactions;
  }

  /**
   * Advances the exchange to the next trading week.
   *
   * <p>For each listed stock the exchange first checks whether a truthful
   * rumor exists about it from the previous week. If so, the random walk
   * is biased in the predicted direction. Then the {@link EventGenerator}
   * is consulted — if an event fires, it overrides the walk entirely.
   * Finally, fresh rumors are generated for the upcoming week.</p>
   */
  public void advance() {
    this.week++;
    this.lastWeekEvents.clear();

    final BigDecimal maxChange = new BigDecimal("0.05");

    Map<String, Rumor.Direction> truthfulRumors = new HashMap<>();
    for (Rumor rumor : this.activeRumors) {
      if (rumor.truthful()) {
        truthfulRumors.put(rumor.stock().getSymbol(), rumor.direction());
      }
    }

    for (Stock stock : this.stockMap.values()) {
      BigDecimal current = stock.getSalesPrice();
      BigDecimal newPrice;

      Optional<MarketEvent> event = this.eventGenerator.sample();
      if (event.isPresent()) {
        MarketEvent fired = event.get();
        newPrice = fired.apply(stock, current);
        this.lastWeekEvents.add(
            new EventNotice(stock, fired.getHeadline(), current, newPrice));
      } else {
        double r = (this.random.nextDouble() * 2.0) - 1.0;
        Rumor.Direction biased = truthfulRumors.get(stock.getSymbol());
        BigDecimal swing = maxChange;

        if (biased == Rumor.Direction.RISE) {
          r = 0.5 + (this.random.nextDouble() * 0.5);
          swing = new BigDecimal("0.10");
        } else if (biased == Rumor.Direction.FALL) {
          r = -(0.5 + (this.random.nextDouble() * 0.5));
          swing = new BigDecimal("0.12");
        }

        BigDecimal change = swing.multiply(BigDecimal.valueOf(r));
        BigDecimal factor = BigDecimal.ONE.add(change);
        newPrice = current.multiply(factor);
      }

      if (newPrice.compareTo(new BigDecimal("0.01")) < 0) {
        newPrice = new BigDecimal("0.01");
      }

      newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);
      stock.addNewSalesPrice(newPrice);
    }

    this.activeRumors = this.rumorGenerator.generate(this.stockMap.values());

    notifyObservers();
  }

  /**
   * Returns the market events that fired during the most recent
   * {@link #advance()} call.
   *
   * @return an unmodifiable list of event notices, empty if no events fired
   */
  public List<EventNotice> getLastWeekEvents() {
    return List.copyOf(this.lastWeekEvents);
  }

  /**
   * Returns rumors currently circulating about next week's price moves.
   *
   * @return an unmodifiable list of active rumors, possibly empty
   */
  public List<Rumor> getActiveRumors() {
    return List.copyOf(this.activeRumors);
  }

  /**
   * Returns the stocks with the largest positive price change since the previous trading week.
   *
   * <p>Only stocks whose sales price has increased are included.
   * The result is sorted by price change in descending order and
   * limited to the given number of stocks.</p>
   *
   * @param limit the maximum number of stocks to return. Must be greater than or equal to 0
   * @return a list of the top gaining stocks, or an empty list if none have increased
   * @throws IllegalArgumentException if {@code limit} is negative
   */
  public List<Stock> getGainers(int limit) {
    validateLimit(limit);

    List<Stock> result = new ArrayList<>(this.stockMap.values());
    result.removeIf(stock -> percentChange(stock).compareTo(BigDecimal.ZERO) <= 0);
    result.sort(
        Comparator.comparing(this::percentChange)
            .reversed()
            .thenComparing(Stock::getSymbol)
    );

    return new ArrayList<>(result.subList(0, Math.min(limit, result.size())));
  }

  /**
   * Returns the stocks with the largest negative price change since the previous trading week.
   *
   * <p>Only stocks whose sales price has decreased are included.
   * The result is sorted by price change in ascending order and
   * limited to the given number og stocks</p>
   *
   * @param limit the maximum number of stocks to return; must be greater than or equal to 0
   * @return a list of the top losing stocks, or an empty list if none have decreased
   * @throws IllegalArgumentException if {@code limit} is negative
   */
  public List<Stock> getLosers(int limit) {
    validateLimit(limit);

    List<Stock> result = new ArrayList<>(this.stockMap.values());
    result.removeIf(stock -> percentChange(stock).compareTo(BigDecimal.ZERO) >= 0);
    result.sort(
        Comparator.comparing(this::percentChange)
            .thenComparing(Stock::getSymbol)
    );

    return new ArrayList<>(result.subList(0, Math.min(limit, result.size())));
  }

  /**
   * Returns the percentage change between the latest and previous registered
   * sales price of the given stock.
   *
   * @param stock the stock to compute the change for
   * @return the percentage change
   */
  private BigDecimal percentChange(Stock stock) {
    List<BigDecimal> prices = stock.getHistoricalPrices();
    if (prices.size() < 2) {
      return BigDecimal.ZERO;
    }
    BigDecimal previous = prices.get(prices.size() - 2);
    if (previous.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal current = prices.getLast();
    return current.subtract(previous)
        .divide(previous, 6, RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"));
  }

  private void validateLimit(int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("Limit cannot be negative");
    }
  }

  /**
   * Restores an exchange from previously saved game data.
   *
   * @param name   the exchange name
   * @param week   the current trading week
   * @param stocks the stocks to populate the exchange with
   * @return an exchange populated from the provided saved state
   */
  public static Exchange restore(String name, int week, List<Stock> stocks) {
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(stocks, "Stocks cannot be null");

    if (week < 1) {
      throw new IllegalArgumentException("Week must be at least 1");
    }

    Exchange exchange = new Exchange(name, stocks);
    exchange.week = week;
    return exchange;
  }
}
