package no.ntnu.idatt2003.group38.calculator;

import no.ntnu.idatt2003.group38.Share;
import no.ntnu.idatt2003.group38.Stock;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Calculates financial values for a share sale transaction
 *
 * This implementation computes:
 *  Gross value (sales price x quantity)
 *  Commission (1% of gross value)
 *  Tax (30% of profit)
 *  Total sale value (gross value - commission - tax)
 *
 * Profit is calculated as gross value - commission - purchase cost
 * No tax is applied if the profit is zero or negative
 *
 * The calculator is based on data provided by a {@link Share} instance and the associated {@link Stock}
 * All monetary values are represented using {@link java.math.BigDecimal} to ensure precise financial calculations
 */
public class SaleCalculator implements TransactionCalculator {

    private final BigDecimal purchasePrice;
    private final BigDecimal salesPrice;
    private final BigDecimal quantity;
    private static final BigDecimal commissionRate = new BigDecimal("0.01");
    private static final BigDecimal taxRate = new BigDecimal("0.30");

    public SaleCalculator(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(share.getStock(), "Share cannot be null");

        this.purchasePrice = share.getPurchasePrice();
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();
    }

    @Override
    public BigDecimal calculateGross() {
        return salesPrice.multiply(quantity);
    }

    @Override
    public BigDecimal calculateCommission() {
        return calculateGross().multiply(commissionRate);
    }

    @Override
    public BigDecimal calculateTax() {
        BigDecimal gross = calculateGross();
        BigDecimal commission = calculateCommission();
        BigDecimal purchaseCost = purchasePrice.multiply(quantity);

        BigDecimal profit = gross
                .subtract(commission)
                .subtract(purchaseCost);

        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return profit.multiply(taxRate);
    }

    @Override
    public BigDecimal calculateTotal() {
        return calculateGross()
                .subtract(calculateCommission())
                .subtract(calculateTax());
    }
}
