package no.ntnu.idatt2003.group38.view.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;

/**
 * The Portfolio page view.
 *
 * <p>Shows the player's aggregated holdings in a table on the left and a
 * detail panel on the right with summary metrics, selected-share information,
 * trade controls and price history for the current selection.</p>
 */
public class PortfolioView {

    private static final String STYLESHEET = "/stylesheets/market.css";

    private final HBox root;
    private final VBox rowsContainer;

    private final Label cashLabel;
    private final Label portfolioValueLabel;
    private final Label netWorthLabel;
    private final Label totalGainLossLabel;

    private final Label symbolLabel;
    private final Label companyLabel;
    private final Label quantityLabel;
    private final Label buyPriceLabel;
    private final Label currentPriceLabel;
    private final Label positionValueLabel;
    private final Label selectedGainLossLabel;
    private final Label selectedAllocationLabel;
    private final Label historyTitle;
    private final LineChart<Number, Number> historyChart;
    private final XYChart.Series<Number, Number> historySeries;

    private final Spinner<Integer> buyQuantitySpinner;
    private final Spinner<Integer> sellQuantitySpinner;

    private final Button buyButton;
    private final Button sellButton;

    private Consumer<Share> onShareSelected = share -> {
    };

    private IntConsumer onBuySelected = quantity -> {
    };
    private IntConsumer onSellSelected = quantity -> {
    };

    private BigDecimal currentPortfolioValue = BigDecimal.ZERO;

    private String selectedSymbol;

    private final DecimalFormat moneyFormat;

    /**
     * Builds the portfolio view with an empty holdings list and no selected share.
     */
    public PortfolioView() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        this.moneyFormat = new DecimalFormat("#,##0", symbols);
        Label title = new Label("Portfolio");
        title.getStyleClass().add("market-title");

        this.rowsContainer = new VBox(8);
        this.rowsContainer.getStyleClass().add("market-rows");
        this.rowsContainer.setFillWidth(true);

        HBox header = buildHeaderRow();

        ScrollPane scrollpane = new ScrollPane(this.rowsContainer);
        scrollpane.setFitToWidth(true);
        scrollpane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollpane.getStyleClass().add("market-scroll");
        VBox.setVgrow(scrollpane, Priority.ALWAYS);

        VBox listPanel = new  VBox(16, title, header, scrollpane);
        listPanel.getStyleClass().add("market-panel");
        HBox.setHgrow(listPanel, Priority.ALWAYS);

        Label summaryTitle = new Label("Portfolio Summary");
        summaryTitle.getStyleClass().add("stock-card-title");

        this.cashLabel = new Label();
        this.cashLabel.getStyleClass().add("stock-card-line");

        this.portfolioValueLabel = new Label();
        this.portfolioValueLabel.getStyleClass().add("stock-card-line");

        this.netWorthLabel = new Label();
        this.netWorthLabel.getStyleClass().add("stock-card-line");

        this.totalGainLossLabel = new Label();
        this.totalGainLossLabel.getStyleClass().add("stock-card-line");

        Label selectedTitle = new Label("Selected Share");
        selectedTitle.getStyleClass().add("stock-card-title");

        this.symbolLabel = new Label();
        this.symbolLabel.getStyleClass().add("stock-card-symbol");

        this.companyLabel = new Label();
        this.companyLabel.getStyleClass().add("stock-card-line");

        this.quantityLabel = new Label();
        this.quantityLabel.getStyleClass().add("stock-card-line");

        this.buyPriceLabel = new Label();
        this.buyPriceLabel.getStyleClass().add("stock-card-line");

        this.currentPriceLabel = new Label();
        this.currentPriceLabel.getStyleClass().add("stock-card-line");

        this.positionValueLabel = new Label();
        this.positionValueLabel.getStyleClass().add("stock-card-line");

        this.selectedGainLossLabel = new Label();
        this.selectedGainLossLabel.getStyleClass().add("stock-card-line");

        this.selectedAllocationLabel = new Label();
        this.selectedAllocationLabel.getStyleClass().add("stock-card-line");

        this.historyTitle = new Label("Price history");
        this.historyTitle.getStyleClass().add("stock-card-title");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Week");
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Price");
        yAxis.setForceZeroInRange(false);

        this.historySeries = new XYChart.Series<>();
        this.historyChart = new LineChart<>(xAxis, yAxis);
        this.historyChart.setLegendVisible(false);
        this.historyChart.setAnimated(false);
        this.historyChart.setCreateSymbols(false);
        this.historyChart.setPrefHeight(180);
        this.historyChart.getData().add(this.historySeries);

        Label buyQuantityTitle = new Label("Buy quantity");
        buyQuantityTitle.getStyleClass().add("stock-card-line");

        this.buyQuantitySpinner = new Spinner<>(1, 10_000, 1);
        this.buyQuantitySpinner.setEditable(true);
        this.buyQuantitySpinner.getStyleClass().add("stock-card-spinner");

        Label sellQuantityTitle = new Label("Sell quantity");
        sellQuantityTitle.getStyleClass().add("stock-card-line");

        this.sellQuantitySpinner = new Spinner<>(1, 1, 1);
        this.sellQuantitySpinner.setEditable(true);
        this.sellQuantitySpinner.getStyleClass().add("stock-card-spinner");

        this.buyButton = new Button("BUY");
        this.buyButton.getStyleClass().add("buy-button");
        this.buyButton.setOnAction(event -> this.onBuySelected.accept(this.buyQuantitySpinner.getValue()));

        this.sellButton = new Button("SELL");
        this.sellButton.getStyleClass().addAll("buy-button", "sell-button");
        this.sellButton.setOnAction(event -> this.onSellSelected.accept(this.sellQuantitySpinner.getValue()));

        HBox actionButtons = new HBox(8, this.buyButton, this.sellButton);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        VBox detailsPanel = new VBox(
                8,
                summaryTitle,
                this.cashLabel,
                this.portfolioValueLabel,
                this.netWorthLabel,
                this.totalGainLossLabel,
                new Separator(),
                selectedTitle,
                this.symbolLabel,
                this.companyLabel,
                this.quantityLabel,
                this.buyPriceLabel,
                this.currentPriceLabel,
                this.positionValueLabel,
                this.selectedGainLossLabel,
                this.selectedAllocationLabel,
                this.historyTitle,
                this.historyChart,
                buyQuantityTitle,
                this.buyQuantitySpinner,
                sellQuantityTitle,
                this.sellQuantitySpinner,
                actionButtons);
        detailsPanel.getStyleClass().add("market-panel");
        detailsPanel.setAlignment(Pos.TOP_LEFT);
        detailsPanel.setPrefWidth(260);
        detailsPanel.setMinWidth(260);

        this.root = new HBox(20, listPanel, detailsPanel);
        this.root.setPadding(new Insets(20));
        this.root.getStyleClass().add("market-view");

        setSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        showSelectedShare(null);
        this.historySeries.getData().clear();
        this.historyTitle.setManaged(false);
        this.historyTitle.setVisible(false);
        this.historyChart.setManaged(false);
        this.historyChart.setVisible(false);
    }

    /**
     * Returns the root node so the controller can mount the view.
     *
     * @return the root layout container of the view
     */
    public Region getRoot() {
        return this.root;
    }

    /**
     * Replaces the rows in the table with one row per aggregated holding.
     *
     * @param shares the holdings to display. Must not be {@code null}
     */
    public  void setShares(List<Share> shares) {
        Objects.requireNonNull(shares, "Shares cannot be null");
        this.rowsContainer.getChildren().clear();

        this.currentPortfolioValue = shares.stream()
                .map(this::calculatePositionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(shares.isEmpty()) {
            Label emptyLabel = new Label("No shares owned yet");
            emptyLabel.getStyleClass().add("stock-card-line");
            this.rowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for(Share share : shares) {
            this.rowsContainer.getChildren().add(buildShareRow(share));
        }
    }

    /**
     * Stores the symbol that should be rendered as selected in the holdings table.
     *
     * @param selectedSymbol the selected stock symbol, or {@code null} if none is selected
     */
    public void setSelectedSymbol(String selectedSymbol) {
        this.selectedSymbol = selectedSymbol;
    }

    /**
     * Updates the right-hand details panel to show the given holding, or clears it
     * if {@code share} is {@code null}.
     *
     * @param share the holding to display, or {@code null} to show the empty state
     */
    public void showSelectedShare(Share share) {
        if (share == null) {
            this.symbolLabel.setText("No share selected");
            this.companyLabel.setText("");
            this.quantityLabel.setText("");
            this.buyPriceLabel.setText("");
            this.currentPriceLabel.setText("");
            this.positionValueLabel.setText("");
            this.selectedGainLossLabel.setText("");
            this.selectedAllocationLabel.setText("");
            this.historySeries.getData().clear();
            this.historyTitle.setManaged(false);
            this.historyTitle.setVisible(false);
            this.historyChart.setManaged(false);
            this.historyChart.setVisible(false);

            this.buyQuantitySpinner.getValueFactory().setValue(1);
            this.sellQuantitySpinner.getValueFactory().setValue(1);
            this.buyQuantitySpinner.setDisable(false);
            this.sellQuantitySpinner.setDisable(false);
            this.buyButton.setDisable(false);
            this.sellButton.setDisable(false);
            applyChangeColor(this.selectedGainLossLabel, BigDecimal.ZERO);
            return;
        }

        BigDecimal currentPrice = share.getStock().getSalesPrice();
        BigDecimal positionValue = new SaleCalculator(share).calculateTotal();
        BigDecimal invested = calculateInvestedValue(share);
        BigDecimal gainLoss = positionValue.subtract(invested);
        BigDecimal gainLossPct = calculatePercent(gainLoss, invested);
        BigDecimal allocationPct = calculateAllocationPercent(positionValue);

        this.symbolLabel.setText(share.getStock().getSymbol());
        this.companyLabel.setText(share.getStock().getCompany());
        this.quantityLabel.setText("Quantity: " + formatQuantity(share.getQuantity()));
        this.buyPriceLabel.setText("Buy price: " + formatMoney(share.getPurchasePrice()));
        this.currentPriceLabel.setText("Current price: " + formatMoney(currentPrice));
        this.positionValueLabel.setText("Value: " + formatMoney(positionValue));
        this.selectedGainLossLabel.setText("Gain/Loss: " + formatSignedMoney(gainLoss) + " (" + formatSignedPercent(gainLossPct) + ")");
        this.selectedAllocationLabel.setText("Allocation: " + formatPercent(allocationPct));

        updateHistoryChart(share);
        this.historyTitle.setManaged(true);
        this.historyTitle.setVisible(true);
        this.historyChart.setManaged(true);
        this.historyChart.setVisible(true);
        this.buyQuantitySpinner.setDisable(false);
        this.sellQuantitySpinner.setDisable(false);
        this.buyButton.setDisable(false);
        this.sellButton.setDisable(false);

        this.buyQuantitySpinner.getValueFactory().setValue(1);
        SpinnerValueFactory.IntegerSpinnerValueFactory sellFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) this.sellQuantitySpinner.getValueFactory();
        int maxSell = share.getQuantity().intValueExact();
        sellFactory.setMax(maxSell);
        sellFactory.setValue(1);
        applyChangeColor(this.selectedGainLossLabel, gainLoss);
    }

    /**
     * Updates the summary panel using zero gain/loss values.
     *
     * @param cash the player's available cash
     * @param portfolioValue the portfolio's current sale value
     * @param netWorth the player's total net worth
     */
    public void setSummary(BigDecimal cash, BigDecimal portfolioValue, BigDecimal netWorth) {
        setSummary(cash, portfolioValue, netWorth, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Updates the summary panel with the latest cash, valuation and gain/loss values.
     *
     * @param cash the player's available cash. Must not be {@code null}
     * @param portfolioValue the portfolio's current sale value. Must not be {@code null}
     * @param netWorth the player's total net worth. Must not be {@code null}
     * @param totalGainLoss the aggregated unrealized gain/loss. Must not be {@code null}
     * @param totalGainLossPct the aggregated unrealized gain/loss percentage. Must not be {@code null}
     */
    public void setSummary(
            BigDecimal cash,
            BigDecimal portfolioValue,
            BigDecimal netWorth,
            BigDecimal totalGainLoss,
            BigDecimal totalGainLossPct) {

        Objects.requireNonNull(cash, "Cash cannot be null");
        Objects.requireNonNull(portfolioValue, "Portfolio value cannot be null");
        Objects.requireNonNull(netWorth, "Net worth cannot be null");
        Objects.requireNonNull(totalGainLoss, "Total gain loss cannot be null");
        Objects.requireNonNull(totalGainLossPct, "Total gain loss Pct cannot be null");

        this.cashLabel.setText("Cash: " + formatMoney(cash));
        this.portfolioValueLabel.setText("Portfolio: " + formatMoney(portfolioValue));
        this.netWorthLabel.setText("Net worth: " + formatMoney(netWorth));
        this.totalGainLossLabel.setText("Total gain/loss: " + formatSignedMoney(totalGainLoss) + " (" + formatSignedPercent(totalGainLossPct) + ")");
        applyChangeColor(this.totalGainLossLabel, totalGainLoss);
    }

    /**
     * Registers the callback to invoke when the user clicks a holding row.
     *
     * @param onShareSelected the callback. Must not be {@code null}
     */
    public void setOnShareSelected(Consumer<Share> onShareSelected) {
        this.onShareSelected = Objects.requireNonNull(onShareSelected, "onShareSelected cannot be null");
    }

    /**
     * Registers the callback to invoke when the user clicks Buy.
     *
     * @param onBuySelected the callback receiving the requested quantity. Must not be {@code null}
     */
    public void setOnBuySelected(IntConsumer onBuySelected) {
        this.onBuySelected = Objects.requireNonNull(onBuySelected, "onBuySelected cannot be null");
    }

    /**
     * Registers the callback to invoke when the user clicks Sell.
     *
     * @param onSellSelected the callback receiving the requested quantity. Must not be {@code null}
     */
    public void setOnSellSelected(IntConsumer onSellSelected) {
        this.onSellSelected = Objects.requireNonNull(onSellSelected, "onSellSelected cannot be null");
    }

    /**
     * Attaches the portfolio stylesheet to the given scene if it is not already present.
     *
     * @param scene the scene to attach the stylesheet to. Must not be {@code null}
     */
    public void attachTo(Scene scene) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        String css = Objects.requireNonNull(getClass().getResource(STYLESHEET), "Could not find stylesheet at " + STYLESHEET).toExternalForm();
        if(!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    // Components

    private HBox buildHeaderRow() {
        Label stockHeader = new Label("Stock");
        Label companyHeader = new Label("Company");
        Label quantityHeader = new Label("Qty");
        Label buyHeader = new Label("Buy");
        Label currentHeader = new Label("Current");
        Label valueHeader = new Label("Value");
        Label gainLossHeader = new Label("P/L");
        Label allocationHeader = new Label("Allocation");

        for (Label label : List.of(
                stockHeader, companyHeader, quantityHeader, buyHeader, currentHeader, valueHeader, gainLossHeader, allocationHeader)) {
            label.getStyleClass().add("market-column-header");
        }

        HBox header = new HBox(
                cell(stockHeader, 80),
                cell(companyHeader, 220),
                cell(quantityHeader, 70),
                cell(buyHeader, 90),
                cell(currentHeader, 90),
                cell(valueHeader, 100),
                cell(gainLossHeader, 150),
                cell(allocationHeader, 90));
        header.getStyleClass().add("market-row-header");
        return header;
    }

    private HBox buildShareRow(Share share) {
        BigDecimal currentPrice = share.getStock().getSalesPrice();
        BigDecimal positionValue = new SaleCalculator(share).calculateTotal();
        BigDecimal invested = calculateInvestedValue(share);
        BigDecimal gainLoss = positionValue.subtract(invested);
        BigDecimal gainLossPct = calculatePercent(gainLoss, invested);
        BigDecimal allocationPct = calculateAllocationPercent(positionValue);

        Label symbol = new Label(share.getStock().getSymbol());
        Label company = new Label(share.getStock().getCompany());
        Label quantity = new Label(formatQuantity(share.getQuantity()));
        Label buyPrice = new Label(formatMoney(share.getPurchasePrice()));
        Label current = new Label(formatMoney(currentPrice));
        Label value = new Label(formatMoney(positionValue));
        Label gainLossLabel = new Label(formatSignedMoney(gainLoss) + " (" + formatSignedPercent(gainLossPct) + ")");
        Label allocationLabel = new Label(formatPercent(allocationPct));
        applyChangeColor(gainLossLabel, gainLoss);

        HBox row = new  HBox(
                cell(symbol, 80),
                cell(company, 220),
                cell(quantity, 70),
                cell(buyPrice, 90),
                cell(current, 90),
                cell(value, 100),
                cell(gainLossLabel, 150),
                cell(allocationLabel, 90));
        row.getStyleClass().add("market-row");
        if (share.getStock().getSymbol().equals(this.selectedSymbol)) {
            row.getStyleClass().add("selected");
        }
        row.setOnMouseClicked(event -> this.onShareSelected.accept(share));
        return row;
    }

    /**
     * Wraps a node in a fixed-width cell so table columns align across rows.
     */
    private HBox cell(Node content, double width) {
        HBox box = new HBox(content);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        return box;
    }

    private BigDecimal calculatePositionValue(Share share) {
        return new SaleCalculator(share).calculateTotal();
    }

    private BigDecimal calculateInvestedValue(Share share) {
        return new PurchaseCalculator(share).calculateTotal();
    }

    /**
     * Returns {@code value} as a percentage of {@code base}, or zero if
     * {@code base} is zero.
     */
    private BigDecimal calculatePercent(BigDecimal value, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(base, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateAllocationPercent(BigDecimal positionValue) {
        if (this.currentPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return positionValue.divide(this.currentPortfolioValue, 4, RoundingMode.HALF_UP). multiply(BigDecimal.valueOf(100));
    }

    /**
     * Rebuilds the price history chart for the selected share's stock.
     *
     * @param share the selected share whose stock history should be displayed
     */
    private void updateHistoryChart(Share share) {
        this.historySeries.getData().clear();
        List<BigDecimal> prices = share.getStock().getHistoricalPrices();

        for (int i = 0; i < prices.size(); i++) {
            this.historySeries.getData().add(
                    new XYChart.Data<>(i + 1, prices.get(i).doubleValue())
            );
        }
    }

    /**
     * Applies positive/negative change styling to the given label.
     *
     * @param label the label to style
     * @param value the signed value that determines the style
     */
    private void applyChangeColor(Label label, BigDecimal value) {
        label.getStyleClass().removeAll("change-positive", "change-negative");
        if (value.signum() > 0) {
            label.getStyleClass().add("change-positive");
        } else if (value.signum() < 0) {
            label.getStyleClass().add("change-negative");
        }
    }

    // Formatting

    private String formatQuantity(BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }

    private String formatMoney(BigDecimal value) {
        return this.moneyFormat.format(value.setScale(0, RoundingMode.HALF_UP));
    }

    private String formatSignedMoney(BigDecimal value) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + formatMoney(value);
    }

    private String formatPercent(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String formatSignedPercent(BigDecimal value) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + formatPercent(value);
    }
}
