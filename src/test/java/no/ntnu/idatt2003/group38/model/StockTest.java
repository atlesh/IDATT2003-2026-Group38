package no.ntnu.idatt2003.group38.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;


public class StockTest {

    @Test
    void constructor_setsFields_andInitialSalesPrice() {
        Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("123.45"));

        assertEquals("AAPL", stock.getSymbol());
        assertEquals("Apple Inc", stock.getCompany());
        assertEquals(new BigDecimal("123.45"), stock.getSalesPrice());
    }

    @Test
    void getSalesPrice_returnsLastAddedPrice() {
        Stock stock = new Stock("TSLA", "Tesla", new BigDecimal("100.00"));

        stock.addNewSalesPrice(new BigDecimal("101.50"));
        stock.addNewSalesPrice(new BigDecimal("99.99"));

        assertEquals(new BigDecimal("99.99"), stock.getSalesPrice());
    }

    @Test
    void constructor_nullSymbol_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Stock(null, "Apple Inc", new BigDecimal("100.00")));
    }

    @Test
    void constructor_blankSymbol_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("", "Apple Inc", new BigDecimal("100.00")));

        assertThrows(IllegalArgumentException.class,
                () -> new Stock("   ", "Apple Inc", new BigDecimal("100.00")));
    }

    @Test
    void constructor_nullCompany_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Stock("SYM", null, new BigDecimal("10.00")));
    }

    @Test
    void constructor_blankCompany_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("SYM", "", new BigDecimal("10.00")));

        assertThrows(IllegalArgumentException.class,
                () -> new Stock("SYM", "  ", new BigDecimal("10.00")));
    }

    @Test
    void constructor_nullSalesPrice_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Stock("SYM", "Company", null));
    }

    @Test
    void addNewSalesPrice_null_throwsNullPointerException() {
        Stock stock = new Stock("AAPL", "Apple Inc", new BigDecimal("100.00"));

        assertThrows(NullPointerException.class,
                () -> stock.addNewSalesPrice(null));
    }
}