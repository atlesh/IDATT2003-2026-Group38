package no.ntnu.idatt2003.group38.controller.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.transaction.Sale;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.transaction.TransactionHistoryView;

/**
 * Controller for the Transaction History page.
 *
 * <p>Connects {@link TransactionHistoryView} to the player's transaction archive.
 * Handles filtering, searching, and selection of transactions, and observes the
 * {@link Exchange} so the page stays synchronized with model updates.</p>
 */
public class TransactionHistoryController implements Page, ModelObserver {

    /**
     * Available filters for the transaction list.
     */
    public enum FilterMode {
        /** Shows all transactions. */
        ALL,
        /** Shows only purchase transactions. */
        BUY,
        /** Shows only sale transactions. */
        SELL
    }

    private final TransactionHistoryView view;
    private final Exchange exchange;
    private final Player player;

    private FilterMode currentFilter = FilterMode.ALL;
    private String searchQuery = "";
    private Transaction selectedTransaction;

    /**
     * Creates a new transaction-history controller.
     *
     * @param exchange the exchange to observe for model updates; must not be {@code null}
     * @param player the player whose transaction history is shown; must not be {@code null}
     */
    public TransactionHistoryController(Exchange exchange, Player player) {
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
        this.player = Objects.requireNonNull(player, "Player cannot be null");

        this.view = new TransactionHistoryView();
        this.view.setOnFilterSelected(this::handleFilterSelected);
        this.view.setOnSearch(this::handleSearch);
        this.view.setOnTransaction(this::handleSelect);
    }

    /**
     * Returns the root node of the page.
     *
     * @return the root region for this page
     */
    @Override
    public Region getRoot() {
        return this.view.getRoot();
    }

    /**
     * Attaches the page to the shell lifecycle.
     *
     * <p>Registers the controller as an observer, attaches the stylesheet if the
     * view is part of a scene, and refreshes the displayed data.</p>
     */
    @Override
    public void onAttach() {
        this.exchange.addObserver(this);
        Scene scene = this.view.getRoot().getScene();
        if (scene != null) {
            this.view.attachTo(scene);
        }
        refresh();
    }

    /**
     * Detaches the page from the shell lifecycle.
     *
     * <p>Removes the controller as an observer from the exchange.</p>
     */
    @Override
    public void onDetach() {
        this.exchange.removeObserver(this);
    }

    /**
     * Refreshes the page after a model update.
     */
    @Override
    public void onModelChanged() {
        refresh();
    }

    private void handleFilterSelected(FilterMode filter) {
        this.currentFilter = Objects.requireNonNull(filter, "Filter cannot be null");
        refresh();
    }

    private void handleSearch(String query) {
        this.searchQuery = query == null ? "" : query;
        refresh();
    }

    private void handleSelect(Transaction transaction) {
        this.selectedTransaction = transaction;
        this.view.showSelectedTransaction(transaction);
    }

    private void refresh() {
        List<Transaction> allTransactions = new ArrayList<>(this.player.getTransactionArchive().getTransactions());

        List<Transaction> filteredTransactions = new ArrayList<>(filterTransactions(allTransactions));
        Collections.reverse(filteredTransactions);

        this.view.setActiveFilter(this.currentFilter);
        this.view.setTransactions(filteredTransactions);
        this.view.setSummary(
                filteredTransactions.size(),
                countPurchases(filteredTransactions),
                countSales(filteredTransactions),
                calculateMoneySpent(filteredTransactions),
                calculateMoneyEarned(filteredTransactions),
                calculateTotalCommission(filteredTransactions),
                calculateTotalTax(filteredTransactions),
                calculateRealizedProfitLoss(filteredTransactions)
        );

        Transaction selected = findSelected(filteredTransactions);
        this.view.showSelectedTransaction(selected);
        if (selected == null) {
            this.selectedTransaction = null;
        }
    }

    private List<Transaction> filterTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .filter(this::matchesFilter)
                .filter(this::matchesSearch)
                .toList();
    }

    private boolean matchesFilter(Transaction transaction) {
        return switch (this.currentFilter) {
            case ALL -> true;
            case BUY -> transaction instanceof Purchase;
            case SELL -> transaction instanceof Sale;
        };
    }

    private boolean matchesSearch(Transaction transaction) {
        String normalized = this.searchQuery.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return true;
        }

        Stock stock = transaction.getShare().getStock();
        String type = transaction instanceof Purchase ? "BUY" : "SALE";

        return stock.getSymbol().toUpperCase().contains(normalized) || stock.getCompany().toUpperCase().contains(normalized)
                || type.contains(normalized) || String.valueOf(transaction.getWeek()).contains(normalized);
    }

    private Transaction findSelected(List<Transaction> transactions) {
        if (this.selectedTransaction == null) {
            return null;
        }

        for (Transaction transaction : transactions) {
            if (transaction == this.selectedTransaction) {
                return transaction;
            }
        }

        return null;
    }

    private int countPurchases(List<Transaction> transactions) {
        return (int) transactions.stream()
                .filter(Purchase.class::isInstance)
                .count();
    }

    private int countSales(List<Transaction> transactions) {
        return (int) transactions.stream()
                .filter(Sale.class::isInstance)
                .count();
    }

    private BigDecimal calculateMoneySpent(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Purchase.class::isInstance)
                .map(transaction -> transaction.getCalculator().calculateTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMoneyEarned(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Sale.class::isInstance)
                .map(transaction -> transaction.getCalculator().calculateTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalCommission(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.getCalculator().calculateCommission())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalTax(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.getCalculator().calculateTax())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRealizedProfitLoss(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Sale.class::isInstance)
                .map(this::calculateRealizedProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRealizedProfitLoss(Transaction transaction) {
        BigDecimal purchaseCost = transaction.getShare().getPurchasePrice().multiply(transaction.getShare().getQuantity());

        return transaction.getCalculator().calculateTotal().subtract(purchaseCost);
    }

    private BigDecimal calculatePricePerShare(Transaction transaction) {
        BigDecimal quantity = transaction.getShare().getQuantity();
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return transaction.getCalculator().calculateGross().divide(quantity, 10,  RoundingMode.HALF_UP);
    }
}
