package no.ntnu.idatt2003.group38.view.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.controller.transaction.TransactionHistoryController.FilterMode;
import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.transaction.Transaction;

public class TransactionHistoryView {

    private static final String STYLESHEET = "/stylesheets/market.css";

    private static final String FILTER_BASE_STYLE =
            "-fx-font-family: 'Roboto';"
                    + "-fx-font-size: 13px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-color: #F2F2F2;"
                    + "-fx-text-fill: #1A1A1A;"
                    + "-fx-background-radius: 8;"
                    + "-fx-padding: 6 14 6 14;"
                    + "-fx-cursor: hand;";

    private static final String FILTER_ALL_ACTIVE_STYLE =
            FILTER_BASE_STYLE + "-fx-background-color: #1A1A1A; -fx-text-fill: #FFFFFF;";

    private static final String FILTER_BUY_ACTIVE_STYLE =
            FILTER_BASE_STYLE + "-fx-background-color: #C62828; -fx-text-fill: #FFFFFF;";

    private static final String FILTER_SELL_ACTIVE_STYLE =
            FILTER_BASE_STYLE + "-fx-background-color: #2E7D32; -fx-text-fill: #FFFFFF;";

    private final HBox root;
    private final VBox rowsContainer;

    private final Button allFilterButton;
    private final Button buyFilterButton;
    private final Button sellFilterButton;

    private final Label totalTransactionsLabel;
    private final Label purchasesLabel;
    private final Label salesLabel;
    private final Label moneySpentLabel;
    private final Label moneyEarnedLabel;
    private final Label totalCommissionLabel;
    private final Label totalTaxLabel;
    private final Label realizedProfitLossLabel;

    private final Label typeValueLabel;
    private final Label stockValueLabel;
    private final Label weekValueLabel;
    private final Label quantityValueLabel;
    private final Label priceValueLabel;
    private final Label grossValueLabel;
    private final Label commissionValueLabel;
    private final Label taxValueLabel;
    private final Label totalValueLabel;

    private Consumer<FilterMode> onFilterSelected = filter -> { };
    private Consumer<String> onSearch = query -> { };
    private Consumer<Transaction> onTransaction =  transaction -> { };

    private FilterMode activeFilter = FilterMode.ALL;
    private List<Transaction> currentTransactions = List.of();
    private Transaction selectedTransaction;

    private final DecimalFormat moneyFormat;

    public TransactionHistoryView() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        this.moneyFormat = new DecimalFormat("#,##0", symbols);

        Label title = new Label("Transactions");
        title.getStyleClass().add("market-title");

        this.allFilterButton = new Button("All");
        this.buyFilterButton = new Button("Buy");
        this.sellFilterButton = new Button("Sale");

        configureFilterButton(this.allFilterButton, FilterMode.ALL);
        configureFilterButton(this.buyFilterButton, FilterMode.BUY);
        configureFilterButton(this.sellFilterButton, FilterMode.SELL);

        HBox filterBar = new HBox(8, this.allFilterButton, this.buyFilterButton, this.sellFilterButton);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search transactions...");
        searchField.getStyleClass().add("market-search");
        searchField.setMaxWidth(220);
        searchField.textProperty().addListener((obs,  oldValue, newValue) -> this.onSearch.accept(newValue));

        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);

        HBox toolbar = new HBox(12, filterBar, toolbarSpacer, searchField);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        this.rowsContainer = new VBox(8);
        this.rowsContainer.getStyleClass().add("market-rows");
        this.rowsContainer.setFillWidth(true);

        HBox header = buildHeaderRow();

        ScrollPane scrollPane = new ScrollPane(this.rowsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("market-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox listPanel =  new VBox(16, title, toolbar, header, scrollPane);
        listPanel.getStyleClass().add("market-panel");
        HBox.setHgrow(listPanel, Priority.ALWAYS);

        Label summaryTitle = new Label("Transaction Summary");
        summaryTitle.getStyleClass().add("stock-card-title");

        this.totalTransactionsLabel = createDetailLabel();
        this.purchasesLabel = createDetailLabel();
        this.salesLabel = createDetailLabel();
        this.moneySpentLabel = createDetailLabel();
        this.moneyEarnedLabel = createDetailLabel();
        this.totalCommissionLabel = createDetailLabel();
        this.totalTaxLabel = createDetailLabel();
        this.realizedProfitLossLabel = createDetailLabel();

        VBox summaryPanel = new VBox(
                8,
                summaryTitle,
                this.totalTransactionsLabel,
                this.purchasesLabel,
                this.salesLabel,
                this.moneySpentLabel,
                this.moneyEarnedLabel,
                this.totalCommissionLabel,
                this.totalTaxLabel,
                this.realizedProfitLossLabel
        );
        summaryPanel.getStyleClass().add("market-panel");

        Label selectedTitle = new Label("Selected Transaction");
        selectedTitle.getStyleClass().add("stock-card-title");

        this.typeValueLabel = new Label();
        this.typeValueLabel.getStyleClass().add("stock-card-symbol");
        this.stockValueLabel = createDetailLabel();
        this.weekValueLabel = createDetailLabel();
        this.quantityValueLabel = createDetailLabel();
        this.priceValueLabel = createDetailLabel();
        this.grossValueLabel = createDetailLabel();
        this.commissionValueLabel = createDetailLabel();
        this.taxValueLabel = createDetailLabel();
        this.totalValueLabel = createDetailLabel();

        VBox selectedPanel = new VBox(
                8,
                selectedTitle,
                this.typeValueLabel,
                this.stockValueLabel,
                this.weekValueLabel,
                this.quantityValueLabel,
                this.priceValueLabel,
                this.grossValueLabel,
                this.commissionValueLabel,
                this.taxValueLabel,
                this.totalValueLabel
        );
        selectedPanel.getStyleClass().add("market-panel");

        VBox rightColumn = new VBox(20, summaryPanel, selectedPanel);
        rightColumn.setPrefWidth(300);
        rightColumn.setMinWidth(300);

        this.root = new HBox(20, listPanel, rightColumn);
        this.root.setPadding(new Insets(20));
        this.root.getStyleClass().add("market-view");

        setActiveFilter(FilterMode.ALL);
        setSummary(0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        showSelectedTransaction(null);
    }

    public Region getRoot() {
        return this.root;
    }

    public void attachTo(Scene scene) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        String css = Objects.requireNonNull(getClass().getResource(STYLESHEET), "Could not find stylesheet at " + STYLESHEET).toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    public void setOnFilterSelected(Consumer<FilterMode> onFilterSelected) {
        this.onFilterSelected = Objects.requireNonNull(onFilterSelected, "onFilterSelected cannot be null");
    }

    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = Objects.requireNonNull(onSearch, "onSearch cannot be null");
    }

    public void setOnTransaction(Consumer<Transaction> onTransaction) {
        this.onTransaction = Objects.requireNonNull(onTransaction, "onTransaction cannot be null");
    }

    public void setActiveFilter(FilterMode activeFilter) {
        this.activeFilter = Objects.requireNonNull(activeFilter, "activeFilter cannot be null");
        updateFilterButtonStyles();
    }

    public void setTransactions(List<Transaction> transactions) {
        Objects.requireNonNull(transactions, "transactions cannot be null");
        this.currentTransactions = List.copyOf(transactions);
        renderTransactions();
    }

    public void setSummary(
            int totalTransactions,
            int purchases,
            int sales,
            BigDecimal moneySpent,
            BigDecimal moneyEarned,
            BigDecimal totalCommission,
            BigDecimal totalTax,
            BigDecimal realizedProfitLoss) {

        this.totalTransactionsLabel.setText("Total transactions: " + totalTransactions);
        this.purchasesLabel.setText("Purchases: " + purchases);
        this.salesLabel.setText("Sales: " + sales);

        BigDecimal spentValue = moneySpent.negate();
        this.moneySpentLabel.setText("Money spent: " + formatSignedMoney(spentValue));
        applyChangeColor(this.moneySpentLabel, spentValue);

        this.moneyEarnedLabel.setText("Money earned: " + formatSignedMoney(moneyEarned));
        applyChangeColor(this.moneyEarnedLabel, moneyEarned);

        BigDecimal commissionValue = totalCommission.negate();
        this.totalCommissionLabel.setText("Total commission: " + formatSignedMoney(commissionValue));
        applyChangeColor(this.totalCommissionLabel, commissionValue);

        BigDecimal taxValue = totalTax.negate();
        this.totalTaxLabel.setText("Total tax: " + formatSignedMoney(taxValue));
        applyChangeColor(this.totalTaxLabel, taxValue);

        this.realizedProfitLossLabel.setText("Realized profit/loss: " + formatSignedMoney(realizedProfitLoss));
        applyChangeColor(this.realizedProfitLossLabel, realizedProfitLoss);
    }

    public void showSelectedTransaction(Transaction transaction) {
        this.selectedTransaction = transaction;
        renderTransactions();

        clearValueStyles();

        if (transaction == null) {
            this.typeValueLabel.setText("No transaction selected");
            this.stockValueLabel.setText("");
            this.weekValueLabel.setText("");
            this.quantityValueLabel.setText("");
            this.priceValueLabel.setText("");
            this.grossValueLabel.setText("");
            this.commissionValueLabel.setText("");
            this.taxValueLabel.setText("");
            this.totalValueLabel.setText("");
            return;
        }

        boolean isPurchase = transaction instanceof Purchase;
        BigDecimal pricePerShare = calculatePricePerShare(transaction);
        BigDecimal gross = transaction.getCalculator().calculateGross();
        BigDecimal commission = transaction.getCalculator().calculateCommission().negate();
        BigDecimal tax = transaction.getCalculator().calculateTax().negate();
        BigDecimal total = isPurchase ? transaction.getCalculator().calculateTotal().negate() : transaction.getCalculator().calculateTotal();

        this.typeValueLabel.setText("Type: " + (isPurchase ? "Buy" : "Sale"));
        applyChangeColor(this.typeValueLabel, isPurchase ? BigDecimal.ONE.negate() : BigDecimal.ONE);

        this.stockValueLabel.setText("Stock: " + transaction.getShare().getStock().getSymbol());
        this.weekValueLabel.setText("Week: " + transaction.getWeek());
        this.quantityValueLabel.setText("Quantity: " + formatQuantity(transaction.getShare().getQuantity()));
        this.priceValueLabel.setText("Price/share: " + formatMoney(pricePerShare));
        this.grossValueLabel.setText("Gross: " + formatMoney(gross));
        this.commissionValueLabel.setText("Commission: " + formatSignedMoney(commission));
        this.taxValueLabel.setText("Tax: " + formatSignedMoney(tax));
        this.totalValueLabel.setText("Total: " + formatSignedMoney(total));

        applyChangeColor(this.commissionValueLabel, commission);
        applyChangeColor(this.taxValueLabel, tax);
        applyChangeColor(this.totalValueLabel, total);
    }

    private void configureFilterButton(Button button, FilterMode mode) {
        button.setStyle(FILTER_BASE_STYLE);
        button.setOnAction(event -> this.onFilterSelected.accept(mode));
    }

    private void updateFilterButtonStyles() {
        this.allFilterButton.setStyle(FILTER_BASE_STYLE);
        this.buyFilterButton.setStyle(FILTER_BASE_STYLE);
        this.sellFilterButton.setStyle(FILTER_BASE_STYLE);

        switch (this.activeFilter) {
            case ALL -> this.allFilterButton.setStyle(FILTER_ALL_ACTIVE_STYLE);
            case BUY -> this.buyFilterButton.setStyle(FILTER_BUY_ACTIVE_STYLE);
            case SELL -> this.sellFilterButton.setStyle(FILTER_SELL_ACTIVE_STYLE);
        }
    }

    private void renderTransactions() {
        this.rowsContainer.getChildren().clear();

        if (this.currentTransactions.isEmpty()) {
            Label emptyLabel = new Label("No transactions found");
            emptyLabel.getStyleClass().add("stock-card-line");
            this.rowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for  (Transaction transaction : this.currentTransactions) {
            this.rowsContainer.getChildren().add(buildTransactionRow(transaction));
        }
    }

    private HBox buildHeaderRow() {
        Label weekheader = new Label("Week");
        Label typeHeader = new Label("Type");
        Label stockHeader = new Label("Stock");
        Label quantityHeader = new Label("Qty");
        Label priceHeader = new Label("Price");
        Label feesHeader = new Label("Fees");
        Label taxHeader = new Label("Tax");
        Label totalHeader = new Label("Total");

        for (Label label : List.of(
                weekheader, typeHeader, stockHeader, quantityHeader, priceHeader, feesHeader, taxHeader, totalHeader)) {
            label.getStyleClass().add("market-column-header");
        }

        HBox header = new HBox(
                cell(weekheader, 55),
                cell(typeHeader, 70),
                cell(stockHeader, 80),
                cell(quantityHeader, 65),
                cell(priceHeader, 90),
                cell(feesHeader, 85),
                cell(taxHeader, 75),
                cell(totalHeader, 95)
        );
        header.getStyleClass().add("market-row-header");
        return header;
    }

    private HBox buildTransactionRow(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;

        BigDecimal pricePerShare = calculatePricePerShare(transaction);
        BigDecimal fees = transaction.getCalculator().calculateCommission().negate();
        BigDecimal tax = transaction.getCalculator().calculateTax().negate();
        BigDecimal total = isPurchase ? transaction.getCalculator().calculateTotal().negate() : transaction.getCalculator().calculateTotal();

        Label week = new Label(String.valueOf(transaction.getWeek()));
        Label type = new Label(isPurchase ? "Buy" : "Sale");
        Label stock = new Label(transaction.getShare().getStock().getSymbol());
        Label quantity = new Label(formatQuantity(transaction.getShare().getQuantity()));
        Label price = new Label(formatMoney(pricePerShare));
        Label feesLabel = new Label(formatSignedMoney(fees));
        Label taxLabel = new Label(formatSignedMoney(tax));
        Label totalLabel = new Label(formatSignedMoney(total));

        applyChangeColor(type, isPurchase ? BigDecimal.ONE.negate() : BigDecimal.ONE);
        applyChangeColor(feesLabel, fees);
        applyChangeColor(taxLabel, tax);
        applyChangeColor(totalLabel, total);

        HBox row = new HBox(
                cell(week, 55),
                cell(type, 70),
                cell(stock, 80),
                cell(quantity, 65),
                cell(price, 90),
                cell(feesLabel, 85),
                cell(taxLabel, 75),
                cell(totalLabel, 95)
        );
        row.getStyleClass().add("market-row");
        row.getStyleClass().add(isPurchase ? "transaction-buy-row" : "transaction-sale-row");

        if (transaction == this.selectedTransaction) {
            row.getStyleClass().add("selected");
        }

        row.setOnMouseClicked(event -> {
            this.selectedTransaction = transaction;
            renderTransactions();
            this.onTransaction.accept(transaction);
        });

        return row;
    }

    private Label createDetailLabel() {
        Label label = new Label();
        label.getStyleClass().add("stock-card-line");
        return label;
    }

    private HBox cell(Node content, double width) {
        HBox box = new HBox(content);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        return box;
    }

    private BigDecimal calculatePricePerShare(Transaction transaction) {
        BigDecimal quantity = transaction.getShare().getQuantity();
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return transaction.getCalculator().calculateGross().divide(quantity, 10, RoundingMode.HALF_UP);
    }

    private void clearValueStyles() {
        for (Label label : List.of(
                this.typeValueLabel,
                this.moneySpentLabel,
                this.moneyEarnedLabel,
                this.totalCommissionLabel,
                this.totalTaxLabel,
                this.realizedProfitLossLabel,
                this.commissionValueLabel,
                this.taxValueLabel,
                this.totalValueLabel
        )) {
            label.getStyleClass().removeAll("change-positive",  "change-negative");
        }
    }

    private void applyChangeColor(Label label, BigDecimal value) {
        label.getStyleClass().removeAll("change-positive",  "change-negative");
        if (value.signum() > 0) {
            label.getStyleClass().add("change-positive");
        } else if (value.signum() < 0) {
            label.getStyleClass().add("change-negative");
        }
    }

    private String formatQuantity(BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }

    private String formatMoney(BigDecimal value) {
        return this.moneyFormat.format(value.setScale(0, RoundingMode.HALF_UP));
    }

    private String formatSignedMoney(BigDecimal value) {
        if (value.signum() > 0) {
            return "+" + formatMoney(value);
        }
        return formatMoney(value);
    }
}