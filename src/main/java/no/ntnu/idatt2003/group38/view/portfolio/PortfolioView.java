package no.ntnu.idatt2003.group38.view.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.function.Consumer;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.view.components.PriceSparkline;

/**
 * The Portfolio page view.
 *
 * <p>Shows the player's aggregated holdings in a table on the left and a
 * detail panel on the right with summary metrics, selected-share information,
 * trade controls and price history for the current selection. The whole page
 * is wrapped in a scroll pane so content remains reachable on small windows.</p>
 */
public class PortfolioView {

  private static final String STYLESHEET = "/stylesheets/market.css";
  private static final Pattern QUANTITY_PATTERN = Pattern.compile("\\d*(\\.\\d*)?");

  private static final String SELL_ALL_DEFAULT_TEXT = "Sell all";
  private static final String SELL_ALL_CONFIRM_TEXT = "Confirm";

  private final ScrollPane root;
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
  private final PriceSparkline historyChart;

  private final TextField buyQuantityField;
  private final TextField sellQuantityField;

  private final Button sellAllButton;
  private Runnable onSellAll = () -> {};
  private boolean sellAllConfirming = false;

  private final Button buyButton;
  private final Button sellButton;

  private final Button analyzeButton;
  private Runnable onAnalyzeSelected = () -> {};

  private Consumer<Share> onShareSelected = share -> {
  };

  private Consumer<BigDecimal> onBuySelected = quantity -> {
  };
  private Consumer<BigDecimal> onSellSelected = quantity -> {
  };

  private BigDecimal currentPortfolioValue = BigDecimal.ZERO;
  private BigDecimal currentSellLimit = BigDecimal.ZERO;
  private boolean hasSelectedShare;

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

    ScrollPane rowsScroll = new ScrollPane(this.rowsContainer);
    rowsScroll.setFitToWidth(true);
    rowsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    rowsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    rowsScroll.getStyleClass().add("market-scroll");
    VBox.setVgrow(rowsScroll, Priority.ALWAYS);

    this.sellAllButton = new Button(SELL_ALL_DEFAULT_TEXT);
    this.sellAllButton.getStyleClass().add("sell-all-button");
    this.sellAllButton.setOnAction(event -> handleSellAllClicked());
    this.sellAllButton.setDisable(true);

    Region titleSpacer = new Region();
    HBox.setHgrow(titleSpacer, Priority.ALWAYS);
    HBox titleRow = new HBox(8, title, titleSpacer, this.sellAllButton);
    titleRow.setAlignment(Pos.CENTER_LEFT);

    VBox listPanel = new VBox(16, titleRow, header, rowsScroll);
    listPanel.getStyleClass().add("market-panel");
    listPanel.setMinWidth(0);
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

    this.historyChart = new PriceSparkline(260, 120);

    Label buyQuantityTitle = new Label("Buy quantity");
    buyQuantityTitle.getStyleClass().add("stock-card-line");

    this.buyQuantityField = createQuantityField();

    Label sellQuantityTitle = new Label("Sell quantity");
    sellQuantityTitle.getStyleClass().add("stock-card-line");

    this.sellQuantityField = createQuantityField();

    this.buyQuantityField.textProperty().addListener((obs, oldValue, newValue) -> updateTradeActionState());
    this.sellQuantityField.textProperty().addListener((obs, oldValue, newValue) -> updateTradeActionState());

    this.buyButton = new Button("BUY");
    this.buyButton.getStyleClass().add("buy-button");
    this.buyButton.setOnAction(event -> {
      BigDecimal quantity = parseQuantity(this.buyQuantityField);
      if (quantity != null) {
        this.onBuySelected.accept(quantity);
      }
    });

    this.sellButton = new Button("SELL");
    this.sellButton.getStyleClass().addAll("buy-button", "sell-button");
    this.sellButton.setOnAction(event -> {
      BigDecimal quantity = parseQuantity(this.sellQuantityField);
      if (quantity != null) {
        this.onSellSelected.accept(quantity);
      }
    });

    this.analyzeButton = new Button("Analyze");
    this.analyzeButton.getStyleClass().add("buy-button");
    this.analyzeButton.setOnAction(event -> this.onAnalyzeSelected.run());

    HBox actionButtons = new HBox(8, this.analyzeButton, this.buyButton, this.sellButton);
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
        this.historyChart.getRoot(),
        buyQuantityTitle,
        this.buyQuantityField,
        sellQuantityTitle,
        this.sellQuantityField,
        actionButtons);
    detailsPanel.getStyleClass().add("market-panel");
    detailsPanel.setAlignment(Pos.TOP_LEFT);
    detailsPanel.setPrefWidth(300);
    detailsPanel.setMinWidth(280);
    detailsPanel.setMaxWidth(320);

    HBox content = new HBox(20, listPanel, detailsPanel);
    content.setPadding(new Insets(20));
    content.getStyleClass().add("market-view");
    HBox.setHgrow(listPanel, Priority.ALWAYS);

    this.root = new ScrollPane(content);
    this.root.setFitToWidth(true);
    this.root.setFitToHeight(true);
    this.root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    this.root.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    this.root.getStyleClass().add("portfolio-scroll");

    setSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    showSelectedShare(null);
    this.historyTitle.setManaged(false);
    this.historyTitle.setVisible(false);
    this.historyChart.getRoot().setManaged(false);
    this.historyChart.getRoot().setVisible(false);
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
  public void setShares(List<Share> shares) {
    Objects.requireNonNull(shares, "Shares cannot be null");
    this.rowsContainer.getChildren().clear();

    this.currentPortfolioValue = shares.stream()
        .map(this::calculatePositionValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (shares.isEmpty()) {
      Label emptyLabel = new Label("No shares owned yet");
      emptyLabel.getStyleClass().add("stock-card-line");
      this.rowsContainer.getChildren().add(emptyLabel);
      return;
    }

    for (Share share : shares) {
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
      this.historyChart.setPrices(java.util.Collections.emptyList());
      this.historyTitle.setManaged(false);
      this.historyTitle.setVisible(false);
      this.historyChart.getRoot().setManaged(false);
      this.historyChart.getRoot().setVisible(false);
      this.hasSelectedShare = false;
      this.currentSellLimit = BigDecimal.ZERO;
      this.buyQuantityField.setText("1");
      this.sellQuantityField.setText("1");
      updateTradeActionState();
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
    this.positionValueLabel.setText("Value: " + formatAmount(positionValue));
    this.selectedGainLossLabel.setText("Gain/Loss: " + formatSignedAmount(gainLoss)
        + " (" + formatSignedPercent(gainLossPct) + ")");
    this.selectedAllocationLabel.setText("Allocation: " + formatPercent(allocationPct));

    this.historyChart.setPrices(share.getStock().getHistoricalPrices());
    this.historyTitle.setManaged(true);
    this.historyTitle.setVisible(true);
    this.historyChart.getRoot().setManaged(true);
    this.historyChart.getRoot().setVisible(true);
    this.hasSelectedShare = true;
    this.currentSellLimit = share.getQuantity();
    this.buyQuantityField.setText("1");
    this.sellQuantityField.setText(formatQuantity(share.getQuantity().min(BigDecimal.ONE)));
    updateTradeActionState();
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
   * @param totalGainLossPct the aggregated unrealized gain/loss percentage.
   *                         Must not be {@code null}
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

    this.cashLabel.setText("Cash: " + formatAmount(cash));
    this.portfolioValueLabel.setText("Portfolio: " + formatAmount(portfolioValue));
    this.netWorthLabel.setText("Net worth: " + formatAmount(netWorth));
    this.totalGainLossLabel.setText("Total gain/loss: " + formatSignedAmount(totalGainLoss)
        + " (" + formatSignedPercent(totalGainLossPct) + ")");
    applyChangeColor(this.totalGainLossLabel, totalGainLoss);
  }

  /**
   * Registers the callback to invoke when the user clicks a holding row.
   *
   * @param onShareSelected the callback. Must not be {@code null}
   */
  public void setOnShareSelected(Consumer<Share> onShareSelected) {
    this.onShareSelected =
        Objects.requireNonNull(onShareSelected, "onShareSelected cannot be null");
  }

  /**
   * Registers the callback to invoke when the user clicks Buy.
   *
   * @param onBuySelected the callback receiving the requested quantity. Must not be {@code null}
   */
  public void setOnBuySelected(Consumer<BigDecimal> onBuySelected) {
    this.onBuySelected =
        Objects.requireNonNull(onBuySelected, "onBuySelected cannot be null");
  }

  /**
   * Registers the callback to invoke when the user clicks Sell.
   *
   * @param onSellSelected the callback receiving the requested quantity. Must not be {@code null}
   */
  public void setOnSellSelected(Consumer<BigDecimal> onSellSelected) {
    this.onSellSelected =
        Objects.requireNonNull(onSellSelected, "onSellSelected cannot be null");
  }

  /**
   * Registers the callback to invoke when the user confirms "Sell all".
   *
   * @param onSellAll the callback. Must not be {@code null}
   */
  public void setOnSellAll(Runnable onSellAll) {
    this.onSellAll = Objects.requireNonNull(onSellAll, "onSellAll cannot be null");
  }

  /**
   * Registers the callback to invoke when the user clicks Analyze.
   *
   * @param onAnalyzeSelected the callback. Must not be {@code null}
   */
  public void setOnAnalyzeSelected(Runnable onAnalyzeSelected) {
    this.onAnalyzeSelected = Objects.requireNonNull(
        onAnalyzeSelected, "onAnalyzeSelected cannot be null");
  }

  /**
   * Enables or disables the "Sell all" button and resets its confirm state.
   *
   * @param hasHoldings {@code true} if the player owns at least one share
   */
  public void setHasHoldings(boolean hasHoldings) {
    this.sellAllButton.setDisable(!hasHoldings);
    resetSellAllButton();
  }

  private void handleSellAllClicked() {
    if (!this.sellAllConfirming) {
      this.sellAllConfirming = true;
      this.sellAllButton.setText(SELL_ALL_CONFIRM_TEXT);
    } else {
      resetSellAllButton();
      this.onSellAll.run();
    }
  }

  private void resetSellAllButton() {
    this.sellAllConfirming = false;
    this.sellAllButton.setText(SELL_ALL_DEFAULT_TEXT);
  }

  /**
   * Attaches the portfolio stylesheet to the given scene if it is not already present.
   *
   * @param scene the scene to attach the stylesheet to. Must not be {@code null}
   */
  public void attachTo(Scene scene) {
    Objects.requireNonNull(scene, "Scene cannot be null");
    String css = Objects.requireNonNull(getClass().getResource(STYLESHEET), "Could not find stylesheet at " + STYLESHEET).toExternalForm();
    if (!scene.getStylesheets().contains(css)) {
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
        cell(stockHeader, 60),
        cell(companyHeader, 140),
        cell(quantityHeader, 50),
        cell(buyHeader, 70),
        cell(currentHeader, 70),
        cell(valueHeader, 80),
        cell(gainLossHeader, 110),
        cell(allocationHeader, 70));
    header.getStyleClass().add("market-row-header");
    header.setMaxWidth(Region.USE_PREF_SIZE);
    return header;
  }

  private TextField createQuantityField() {
    TextField field = new TextField("1");
    field.getStyleClass().add("quantity-input");
    field.setTextFormatter(new TextFormatter<>(change ->
        QUANTITY_PATTERN.matcher(change.getControlNewText()).matches() ? change : null));
    return field;
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
    Label value = new Label(formatAmount(positionValue));
    Label gainLossLabel = new Label(formatSignedAmount(gainLoss)
        + " (" + formatSignedPercent(gainLossPct) + ")");
    Label allocationLabel = new Label(formatPercent(allocationPct));
    applyChangeColor(gainLossLabel, gainLoss);

    HBox row = new HBox(
        cell(symbol, 60),
        cell(company, 140),
        cell(quantity, 50),
        cell(buyPrice, 70),
        cell(current, 70),
        cell(value, 80),
        cell(gainLossLabel, 110),
        cell(allocationLabel, 70));
    row.getStyleClass().add("market-row");
    row.setMaxWidth(Region.USE_PREF_SIZE);
    if (share.getStock().getSymbol().equals(this.selectedSymbol)) {
      row.getStyleClass().add("selected");
    }
    row.setOnMouseClicked(event -> this.onShareSelected.accept(share));
    return row;
  }

  /**
   * Wraps a node in a fixed-width cell so table columns align across rows.
   * Sets a soft minimum so cells can shrink gracefully on narrow windows.
   */
  private HBox cell(Node content, double width) {
    HBox box = new HBox(content);
    box.setAlignment(Pos.CENTER_LEFT);
    box.setPrefWidth(width);
    box.setMinWidth(40);
    return box;
  }

  private void updateTradeActionState() {
    BigDecimal buyQuantity = parseQuantity(this.buyQuantityField);
    BigDecimal sellQuantity = parseQuantity(this.sellQuantityField);

    this.buyQuantityField.setDisable(!this.hasSelectedShare);
    this.sellQuantityField.setDisable(!this.hasSelectedShare);
    this.analyzeButton.setDisable(!this.hasSelectedShare);
    this.buyButton.setDisable(!this.hasSelectedShare || buyQuantity == null);
    this.sellButton.setDisable(!this.hasSelectedShare
        || sellQuantity == null
        || sellQuantity.compareTo(this.currentSellLimit) > 0);
  }

  private BigDecimal parseQuantity(TextField field) {
    String raw = field.getText();
    if (raw == null) {
      return null;
    }

    String trimmed = raw.trim();
    if (trimmed.isEmpty() || ".".equals(trimmed)) {
      return null;
    }

    try {
      BigDecimal quantity = new BigDecimal(trimmed);
      return quantity.compareTo(BigDecimal.ZERO) > 0 ? quantity : null;
    } catch (NumberFormatException e) {
      return null;
    }
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
    return positionValue.divide(this.currentPortfolioValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
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

  private String formatAmount(BigDecimal value) {
    DecimalFormat fmt = new DecimalFormat("#,##0.00", this.moneyFormat.getDecimalFormatSymbols());
    return fmt.format(value);
  }

  private String formatSignedAmount(BigDecimal value) {
    String sign = value.signum() > 0 ? "+" : "";
    return sign + formatAmount(value);
  }

  private String formatPercent(BigDecimal value) {
    return value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
  }

  private String formatSignedPercent(BigDecimal value) {
    String sign = value.signum() > 0 ? "+" : "";
    return sign + formatPercent(value);
  }
}
