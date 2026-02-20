package no.ntnu.idatt2003.group38;

import java.math.BigDecimal;

/**
 * Defines a contract for calculating financial values related to a stock transaction
 *
 * Implemented by different transaction types such as purchases and sales
 */
public interface TransactionCalculator {

    /**
     * Calculates the gross value before fees and taxes
     *
     * @return gross transaction value
     */
    BigDecimal calculateGross();

    /**
     * Calculates the broker commission for the transaction
     *
     * @return commission amount
     */
    BigDecimal calculateCommission();

    /**
     * Calculates the tax associated with the transaction
     *
     * @return tax amount
     */
    BigDecimal calculateTax();

    /**
     * Calculates the total transaction value after fees and taxes
     *
     * @return total transaction value
     */
    BigDecimal calculateTotal();
}
