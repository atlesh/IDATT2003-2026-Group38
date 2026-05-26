package no.ntnu.idatt2003.group38.calculator;

import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;

import java.math.BigDecimal;
import java.util.Objects;
/**
 * Calculates financial values for a share sale transaction
 *
 * <p>This implementation computes:
 *  Gross value (sales price x quantity)
 *  Commission (1% of gross value)
 *  Tax (30% of profit)
 *  Total sale value (gross value - commission - tax)
 * Profit is calculated as gross value - commission - purchase cost
 * No tax is applied if the profit is zero or negative</p>
 *
 * <p>The calculator is based on data provided by a {@link Share}
 * instance and the associated {@link Stock}
 * All monetary values are represented using {@link java.math.BigDecimal}
 * to ensure precise financial calculations</p>
 */
public class SaleCalculator implements TransactionCalculator {

    private final BigDecimal purchasePrice;
    private final BigDecimal salesPrice;
    private final BigDecimal quantity;
    private static final BigDecimal commissionRate = new BigDecimal("0.01");
    private static final BigDecimal taxRate = new BigDecimal("0.30");

    /**
     * Creates a sale calculator for the given share
     *
     * @param share the share to calculate values for; must not be {@code null}
     * @throws NullPointerException if {@code share} is {@code null}
     */
    public SaleCalculator(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(share.getStock(), "Share stock cannot be null");

        this.purchasePrice = share.getPurchasePrice();
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();
    }

    /**
     * Creates a sale calculator using an explicit historical sale price.
     *
     * <p>This constructor is intended for restored transactions where the sale
     * must be recalculated from the saved price rather than the stock's current
     * price on the exchange.</p>
     *
     * @param share the share to calculate values for; must not be {@code null}
     * @param salesPrice the saved sale price to use; must not be {@code null}
     * @throws NullPointerException if {@code share}, its stock, or {@code salesPrice} is {@code null}
     */
    public SaleCalculator(Share share, BigDecimal salesPrice) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(share.getStock(), "Share stock cannot be null");
        Objects.requireNonNull(salesPrice, "Sale price cannot be null");

        this.purchasePrice = share.getPurchasePrice();
        this.salesPrice = salesPrice;
        this.quantity = share.getQuantity();
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal calculateGross() {
        return this.salesPrice.multiply(this.quantity);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal calculateCommission() {
        return calculateGross().multiply(commissionRate);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal calculateTax() {
        BigDecimal gross = calculateGross();
        BigDecimal commission = calculateCommission();
        BigDecimal purchaseCost = this.purchasePrice.multiply(this.quantity);

        BigDecimal profit = gross
                .subtract(commission)
                .subtract(purchaseCost);

        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return profit.multiply(taxRate);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal calculateTotal() {
        return calculateGross()
                .subtract(calculateCommission())
                .subtract(calculateTax());
    }
}
