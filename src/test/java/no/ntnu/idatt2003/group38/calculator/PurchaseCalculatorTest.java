package no.ntnu.idatt2003.group38.calculator;

import no.ntnu.idatt2003.group38.Share;
import no.ntnu.idatt2003.group38.Stock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class PurchaseCalculatorTest {

    private Stock createStock() {
        return new Stock("AAPL", "Apple", new BigDecimal("150"));
    }

    //POSITIVE TESTS

    @Test
    void calculateGross_validShare_returnsPurchasePriceTimesQuantity() {
        Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("150"));
        PurchaseCalculator calculator = new PurchaseCalculator(share);

        assertEquals(new BigDecimal("1500"), calculator.calculateGross());
    }

    @Test
    void calculateCommission_validShare_returnsZeroPointFivePercentOfGross() {
        Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("150"));
        PurchaseCalculator calculator = new PurchaseCalculator(share);

        assertEquals(new BigDecimal("7.500"), calculator.calculateCommission());
    }

    @Test
    void calculateTax_validShare_returnsZero() {
        Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("150"));
        PurchaseCalculator calculator = new PurchaseCalculator(share);

        assertEquals(BigDecimal.ZERO, calculator.calculateTax());
    }

    @Test
    void calculateTotal_validShare_returnsGrossPlusCommission() {
        Share share = new Share(createStock(), new BigDecimal("10"), new BigDecimal("150"));
        PurchaseCalculator calculator = new PurchaseCalculator(share);

        assertEquals(new BigDecimal("1507.500"), calculator.calculateTotal());
    }

    //NEGATIVE TESTS

    @Test
    void constructor_nullShare_throwsException() {
        assertThrows(NullPointerException.class, () -> new PurchaseCalculator(null));
    }
}
