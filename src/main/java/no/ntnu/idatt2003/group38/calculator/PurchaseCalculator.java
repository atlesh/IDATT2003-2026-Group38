package no.ntnu.idatt2003.group38.calculator;

import no.ntnu.idatt2003.group38.model.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Calculates financial values for a share purchase transaction
 *
 * This implementation computes:
 *  Gross value (purchase price x quantity)
 *  Commission (0.5% of gross value)
 *  No tax (tax is not applied on purchases)
 *  Total purchase cost (gross value + commission)
 *
 *  The calculator is based on data provided by a {@link Share} instance
 *  All monetary values are represented using {@link java.math.BigDecimal} to ensure precise financial calculations
 */
public class PurchaseCalculator implements TransactionCalculator {

    private final BigDecimal purchasePrice;
    private final BigDecimal quantity;
    private static final BigDecimal commissionRate = new BigDecimal("0.005");

    public PurchaseCalculator(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");

        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
    }

    @Override
    public BigDecimal calculateGross() {
        return purchasePrice.multiply(quantity);
    }

    @Override
    public BigDecimal calculateCommission() {
        return calculateGross().multiply(commissionRate);
    }

    @Override
    public BigDecimal calculateTax() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTotal() {
        return calculateGross()
                .add(calculateCommission()
                .add(calculateTax()));
    }
}