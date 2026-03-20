package no.ntnu.idatt2003.group38.calculator;

import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class SaleCalculatorTest {

    private Stock createStockWithSalesPrice(BigDecimal salesPrice) {
        return new Stock("AAPL", "Apple", salesPrice);
    }

    //POSITIVE TESTS

    @Test
    void calculateGross_validShare_returnsSalesPriceTimesQuantity() {
        Stock stock = createStockWithSalesPrice(new BigDecimal("200"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(new BigDecimal("2000"), calculator.calculateGross());
    }

    @Test
    void calculateCommission_validShare_returnsOnePercentOfGross() {
        Stock stock = createStockWithSalesPrice(new BigDecimal("200"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(new BigDecimal("20.00"), calculator.calculateCommission());
    }

    @Test
    void calculateTax_positiveProfit_returnsThirtyPercentOfProfit() {
        Stock stock = createStockWithSalesPrice(new BigDecimal("200"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(new BigDecimal("144.0000"), calculator.calculateTax());
    }

    @Test
    void calculateTax_zeroProfit_returnsZero() {
        Stock stock = createStockWithSalesPrice(new BigDecimal("151"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(BigDecimal.ZERO, calculator.calculateTax());
    }

    @Test
    void calculateTotal_positiveProfit_returnsGrossMinusCommissionMinusTax() {
        Stock stock = createStockWithSalesPrice(new BigDecimal("200"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("150"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(new BigDecimal("1836.0000"), calculator.calculateTotal());
    }

    //NEGATIVE TESTS

    @Test
    void constructor_nullShare_throwsException() {
        assertThrows(NullPointerException.class, () -> new SaleCalculator(null));
    }
}