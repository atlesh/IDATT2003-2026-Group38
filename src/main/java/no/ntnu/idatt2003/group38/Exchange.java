package no.ntnu.idatt2003.group38;

import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.transaction.Sale;
import no.ntnu.idatt2003.group38.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Represents a stock exchange where stocks are listed and can be traded by players
 *
 * <p>The exchange maintains a collection of stocks and
 * tracks the current trading week</p>
 *
 * <p>Players can buy and sell shares through the exchange,
 * and the market can advance to simulate price changes</p>
 */
public class Exchange {

    private final String name;
    private int week;
    private final Map<String, Stock> stockMap;
    private final Random random;

    /**
     * Creates a new stock exchange with the given name and listed stocks
     *
     * @param name the name of the exchange; must not be {@code null}
     * @param stocks the list of stocks listed in the exchange; must not be {@code null}
     * @throws NullPointerException if {@code name} or {@code stocks} is {@code null}
     */
    public Exchange(String name, List<Stock> stocks) {
        Objects.requireNonNull(stocks, "Stocks cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.week = 1;
        this.random = new Random();
        this.stockMap = new HashMap<>();

        for (Stock stock : stocks) {
            stockMap.put(stock.getSymbol().toUpperCase(), stock);
        }
    }

    /**
     * Returns the name of the exchange
     *
     * @return the exchange name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current trading week
     *
     * @return the current week number
     */
    public int getWeek() {
        return week;
    }

    /**
     * Checks whether a stock with the given symbol is listed on the exchange
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

        return stockMap.containsKey(normalized);
    }

    /**
     * Returns the stock with the given symbol
     *
     * @param symbol the stock symbol; must not be {@code null} or empty
     * @return the corresponding {@link Stock}
     * @throws IllegalArgumentException if the symbol is {@code null}, empty, or not listed in the exchange
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
            throw new IllegalArgumentException("Stock with symbol '" + normalized + "' does not exist on this exchange");
        }

        return stock;
    }

    /**
     * Searches for stocks whose symbol or company name contains the given search term
     *
     * @param searchTerm the term to search for, or an empty list if none match
     * @return a list of matching stocks, or an empty list if none match
     * @throws IllegalArgumentException if {@code searchTerm} is {@code null}
     */
    public List<Stock> findStocks(String searchTerm) {
        if (searchTerm == null) {
            throw new IllegalArgumentException("Search term cannot be null");
        }

        String normalized = searchTerm.trim().toUpperCase();

        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }

        List<Stock> result = new ArrayList<>();

        for (Stock stock : stockMap.values()) {
            String symbol = stock.getSymbol().toUpperCase();
            String company = stock.getCompany().toUpperCase();

            if (symbol.contains(normalized) || company.contains(normalized)) {
                result.add(stock);
            }
        }
        return result;
    }

    /**
     * Buys a specified quantity of a stock for the given player
     *
     * @param symbol the stock symbol; must not be {@code null}
     * @param quantity the quantity to buy; must be greater than zero
     * @param player the player performing the purchase; must not be {@code null}
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

        Purchase purchase = new Purchase(share, week);
        purchase.commit(player);

        return purchase;
    }

    /**
     * Sells the given share on behalf of the specified player
     *
     * @param share the share to be sold; must not be {@code null}
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

        Sale sale = new Sale(share, week);
        sale.commit(player);

        return sale;
    }

    /**
     * Advances the exchange to the next trading week
     *
     * <p>Increments the week number and applies a small random percentage
     * change to the sales price of each listed stock</p>
     */
    public void advance() {
        week++;

        final BigDecimal maxChange = new BigDecimal("0.05");

        for (Stock stock : stockMap.values()) {
            BigDecimal current = stock.getSalesPrice();

            double r = (random.nextDouble() * 2.0) - 1.0;
            BigDecimal change = maxChange.multiply(BigDecimal.valueOf(r));

            BigDecimal factor = BigDecimal.ONE.add(change);
            BigDecimal newPrice = current.multiply(factor);

            if (newPrice.compareTo(new BigDecimal("0.01")) < 0) {
                newPrice = new BigDecimal("0.01");
            }

            newPrice = newPrice.setScale(2, RoundingMode.HALF_UP);

            stock.addNewSalesPrice(newPrice);
        }
    }
}