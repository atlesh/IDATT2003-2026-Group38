package no.ntnu.idatt2003.group38.controller.analysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.view.analysis.StockAnalysisView;
import no.ntnu.idatt2003.group38.view.shell.ShellView;

/**
 * Controller for {@link StockAnalysisView}.
 *
 * <p>Builds and presents a stock-analysis modal for one selected stock.</p>
 */
public class StockAnalysisController {

    private final StockAnalysisView view;
    private final Player player;
    private final ShellView shell;
    private final Stock stock;
    private final DecimalFormat moneyFormat;

    /**
     * Creates a new stock-analysis controller.
     *
     * @param player the current player. Must not be {@code null}
     * @param shell the shell used to show the modal. Must not be {@code null}
     * @param stock the stock to analyse. Must not be {@code null}
     */
    public StockAnalysisController(Player player, ShellView shell, Stock stock) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.shell = Objects.requireNonNull(shell, "Shell cannot be null");
        this.stock = Objects.requireNonNull(stock, "Stock cannot be null");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        this.moneyFormat = new DecimalFormat("#,##0.00", symbols);

        this.view = new StockAnalysisView();
        this.view.setOnClose(this.shell::hideModal);

        refresh();
    }

    /**
     * Shows the stock-analysis modal in the shell.
     */
    public void show() {
        this.shell.showModal(this.view.getRoot());
    }

    /**
     * Rebuilds the displayed analysis data for the selected stock.
     */
    private void refresh() {
        List<Share> ownedLots = findOwnedLots();

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (Share share : ownedLots) {
            totalQuantity = totalQuantity.add(share.getQuantity());
            totalInvested = totalInvested.add(new PurchaseCalculator(share).calculateTotal());
            totalCurrentValue = totalCurrentValue.add(new SaleCalculator(share).calculateTotal());
        }

        BigDecimal gainLoss = totalCurrentValue.subtract(totalInvested);
        BigDecimal gainLossPercent = calculatePercent(gainLoss, totalInvested);

        this.view.setHeader(this.stock.getSymbol(), this.stock.getCompany());

        this.view.setMarketStats(
                formatMoney(this.stock.getSalesPrice()),
                formatSignedPercent(this.stock.getLatestPriceChangePercent()),
                formatMoney(this.stock.getHighestPrice()),
                formatMoney(this.stock.getLowestPrice())
        );

        this.view.setPositionStats(
                formatQuantity(totalQuantity),
                formatMoney(totalInvested),
                formatMoney(totalCurrentValue),
                formatSignedMoney(gainLoss) + " (" + formatSignedPercent(gainLossPercent) + ")"
        );
        this.view.setOwnershipState(totalQuantity.compareTo(BigDecimal.ZERO) == 0
                ? "You do not currently own this stock."
                : "");

        this.view.setPriceHistory(this.stock.getHistoricalPrices());
        this.view.setRecentTransactions(buildRecentTransactions());
    }

    /**
     * Returns the player's currently owned lots for the selected stock.
     *
     * @return a list of owned lots for the selected stock
     */
    private List<Share> findOwnedLots() {
        return new ArrayList<>(this.player.getPortfolio().getShares(this.stock.getSymbol()));
    }

    /**
     * Builds a short list of recent transactions for the selected stock.
     *
     * @return up to five formatted transaction rows
     */
    private List<String> buildRecentTransactions() {
        List<Transaction> matching = new ArrayList<>();

        for (Transaction transaction : this.player.getTransactionArchive().getTransactions()) {
            if (transaction.getShare().getStock().getSymbol().equals(this.stock.getSymbol())) {
                matching.add(transaction);
            }
        }

        if (matching.isEmpty()) {
            return List.of("No transactions for this stock yet");
        }

        List<String> rows = new ArrayList<>();
        for (int i = matching.size() - 1; i >= 0 && rows.size() < 5; i--) {
            Transaction transaction = matching.get(i);
            String type = transaction instanceof Purchase ? "Buy" : "Sale";
            String quantity = formatQuantity(transaction.getShare().getQuantity());
            String unitPrice = formatMoney(extractUnitPrice(transaction));
            String total = formatMoney(transaction.getCalculator().calculateTotal());

            rows.add("W" + transaction.getWeek()
                    + "  " + type
                    + "  " + quantity
                    + " @ " + unitPrice
                    + "  Total " + total);
        }

        return rows;
    }

    /**
     * Returns the historical unit price represented by the given transaction.
     *
     * @param transaction the transaction to inspect
     * @return the unit price used for the transaction
     */
    private BigDecimal extractUnitPrice(Transaction transaction) {
        BigDecimal quantity = transaction.getShare().getQuantity();
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return transaction.getCalculator().calculateGross()
                .divide(quantity, 10, RoundingMode.HALF_UP);
    }

    /**
     * Returns {@code value} as a percentage of {@code base}, or zero if
     * {@code base} is zero.
     *
     * @param value the value to compare against the base
     * @param base the base value
     * @return the percentage representation of {@code value} relative to {@code base}
     */
    private BigDecimal calculatePercent(BigDecimal value, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(base, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Formats a monetary value for presentation.
     *
     * @param value the value to format
     * @return a formatted money string
     */
    private String formatMoney(BigDecimal value) {
        return this.moneyFormat.format(value.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Formats a signed monetary value for presentation.
     *
     * @param value the value to format
     * @return a formatted signed money string
     */
    private String formatSignedMoney(BigDecimal value) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + formatMoney(value);
    }

    /**
     * Formats a signed percentage value for presentation.
     *
     * @param value the value to format
     * @return a formatted signed percentage string
     */
    private String formatSignedPercent(BigDecimal value) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    /**
     * Formats a quantity value for presentation.
     *
     * @param quantity the quantity to format
     * @return a formatted quantity string without unnecessary trailing zeros
     */
    private String formatQuantity(BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }
}
