package no.ntnu.idatt2003.group38.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stock listed on the exchange
 *
 * <p>A stock has a symbol, a company name and a history of sales prices</p>
 */
public class Stock {

    private final String symbol;
    private final String company;
    private final List<BigDecimal> prices;

    /**
     * Creates a new stock with an initial sales price
     *
     * @param symbol the stock symbol; must not be {@code null} or blank
     * @param company the company name; must not be {@code null} or blank
     * @param salesPrice the initial sales price; must not be {@code null}
     * @throws NullPointerException if {@code symbol}, {@code company},
     *                              or {@code salesPrice} is {@code null}
     * @throws IllegalArgumentException if {@code symbol} or {@code company} is blank
     */
    public Stock(String symbol, String company, BigDecimal salesPrice ) {
        this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }

        this.company = Objects.requireNonNull(company, "company cannot be null");
        if (company.isBlank()) {
            throw new IllegalArgumentException("Company cannot be blank");
        }
        Objects.requireNonNull(salesPrice, "salesPrice cannot be null");

        this.prices = new ArrayList<>();
        this.prices.add(salesPrice);
    }

    /**
     * Returns the stock symbol
     *
     * @return the stock symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the company name
     *
     * @return the company name
     */
    public String getCompany() {
        return company;
    }

    /**
     * Returns the current sales price
     *
     * @return the most recent sales price
     */
    public BigDecimal getSalesPrice() {
        return prices.get(prices.size() - 1);
    }

    /**
     * Checks whether the stock has a previous sales price
     *
     * @return {@code true} if a previous sales price exists, {@code false} otherwise
     */
    public boolean hasPreviousSalesPrice() {
        return prices.size() > 1;
    }

    /**
     * Returns the previous sales price
     *
     * @return the sales price from the previous update
     * @throws IllegalStateException if no previous sales price exists
     */
    public BigDecimal getPreviousSalesPrice() {
        if (!hasPreviousSalesPrice()) {
            throw new IllegalStateException("No previous sales price available");
        }

        return prices.get(prices.size() - 2);
    }

    /**
     * Returns the price change since the previous sales price
     *
     * @return the difference between the current and previous sales price,
     *             or {@link BigDecimal#ZERO} if no previous sales price exists
     */
    public BigDecimal getWeeklyPriceChange() {
        if (!hasPreviousSalesPrice()) {
            return BigDecimal.ZERO;
        }

        return getSalesPrice().subtract(getPreviousSalesPrice());
    }

    /**
     * Adds a new sales price to the price history
     *
     * @param price the new sales price; must not be {@code null}
     * @throws NullPointerException if {@code price} is {@code null}
     */
    public void addNewSalesPrice(BigDecimal price) {
        Objects.requireNonNull(price, "price cannot be null");
        prices.add(price);
    }
}
