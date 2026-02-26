package no.ntnu.idatt2003.group38;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Stock {

    private final String symbol;
    private final String company;
    private final List<BigDecimal> prices;

    public Stock(String symbol, String company, BigDecimal salesPrices ) {
        this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }

        this.company = Objects.requireNonNull(company, "company cannot be null");
        if (company.isBlank()) {
            throw new IllegalArgumentException("Company cannot be blank");
        }
        Objects.requireNonNull(salesPrices, "salesPrice cannot be null");

        this.prices = new ArrayList<>();
        this.prices.add(salesPrices);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompany() {
        return company;
    }

    public BigDecimal getSalesPrice() {
        return prices.get(prices.size() - 1);
    }

    public void addNewSalesPrice(BigDecimal price) {
        Objects.requireNonNull(price, "price cannot be null");
        prices.add(price);
    }
}
