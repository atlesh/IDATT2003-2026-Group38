package no.ntnu.idatt2003.group38.view.analysis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.view.components.PriceSparkline;

/**
 * Modal view that shows detailed analysis for a single stock.
 *
 * <p>The view presents market statistics, the player's current position,
 * price history and recent transactions for the selected stock.</p>
 */
public class StockAnalysisView {

  private static final String STYLESHEET = "/stylesheets/market.css";

  private final ScrollPane root;
  private final VBox content;

  private final Label titleLabel;
  private final Label companyLabel;
  private final Label priceLabel;
  private final Label changeLabel;
  private final Label highLabel;
  private final Label lowLabel;
  private final Label quantityLabel;
  private final Label investedLabel;
  private final Label currentValueLabel;
  private final Label gainLossLabel;
  private final Label ownershipStateLabel;

  private final PriceSparkline historyChart;
  private final VBox recentTransactionsBox;

  private final Button closeButton;

  /**
   * Builds the stock analysis view with empty placeholder values.
   */
  public StockAnalysisView() {
    this.titleLabel = new Label("No stock selected");
    this.titleLabel.getStyleClass().add("market-title");

    this.companyLabel = new Label();
    this.companyLabel.getStyleClass().add("stock-card-line");

    Label marketSectionTitle = new Label("Market Overview");
    marketSectionTitle.getStyleClass().add("stock-card-title");

    this.priceLabel = createLineLabel();
    this.changeLabel = createLineLabel();
    this.highLabel = createLineLabel();
    this.lowLabel = createLineLabel();

    VBox marketSection = new VBox(
        8,
        marketSectionTitle,
        this.priceLabel,
        this.changeLabel,
        this.highLabel,
        this.lowLabel
    );

    marketSection.getStyleClass().addAll("market-panel", "stock-card");
    marketSection.setAlignment(Pos.TOP_LEFT);

    Label positionSectionTitle = new Label("Your Position");
    positionSectionTitle.getStyleClass().add("stock-card-title");

    this.quantityLabel = createLineLabel();
    this.investedLabel = createLineLabel();
    this.currentValueLabel = createLineLabel();
    this.gainLossLabel = createLineLabel();
    this.ownershipStateLabel = createLineLabel();
    this.ownershipStateLabel.setVisible(false);
    this.ownershipStateLabel.setManaged(false);

    VBox positionSection = new VBox(
        8,
        positionSectionTitle,
        this.ownershipStateLabel,
        this.quantityLabel,
        this.investedLabel,
        this.currentValueLabel,
        this.gainLossLabel
    );

    positionSection.getStyleClass().addAll("market-panel", "stock-card");
    positionSection.setAlignment(Pos.TOP_LEFT);

    Label historySectionTitle = new Label("Price History");
    historySectionTitle.getStyleClass().add("stock-card-title");

    this.historyChart = new PriceSparkline(420, 189);

    VBox historySection = new VBox(
        12,
        historySectionTitle,
        this.historyChart.getRoot()
    );

    historySection.getStyleClass().add("market-panel");
    historySection.setAlignment(Pos.TOP_LEFT);

    Label transactionsSectionTitle = new Label("Recent Transactions");
    transactionsSectionTitle.getStyleClass().add("stock-card-title");

    this.recentTransactionsBox = new VBox(8);

    VBox transactionsSection = new VBox(
        12,
        transactionsSectionTitle,
        this.recentTransactionsBox
    );

    transactionsSection.getStyleClass().add("market-panel");
    transactionsSection.setAlignment(Pos.TOP_LEFT);

    this.closeButton = new Button("Close");
    this.closeButton.getStyleClass().add("buy-button");

    VBox header = new VBox(6, this.titleLabel, this.companyLabel);
    header.setAlignment(Pos.TOP_LEFT);

    this.content = new VBox(
        20,
        header,
        marketSection,
        positionSection,
        historySection,
        transactionsSection,
        this.closeButton
    );

    this.content.setPadding(new Insets(20));
    this.content.setAlignment(Pos.TOP_LEFT);
    this.content.getStyleClass().add("market-view");

    this.root = new ScrollPane(this.content);
    this.root.setFitToWidth(true);
    this.root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    this.root.getStyleClass().add("market-scroll");

    setHeader("No stock selected", "");
    setMarketStats("-", "-", "-", "-");
    setPositionStats("0", "0", "0", "0");
    setPriceHistory(List.of());
    setRecentTransactions(List.of("No transactions for this stock yet"));
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
   * Attaches the analysis stylesheet to the given scene.
   *
   * @param scene the scene to attach the stylesheet to. Must not be {@code null}
   */
  public void attachTo(Scene scene) {
    Objects.requireNonNull(scene, "Scene cannot be null");
    String css = Objects.requireNonNull(getClass().getResource(STYLESHEET),
        "Could not find stylesheet at " + STYLESHEET).toExternalForm();

    if (!scene.getStylesheets().contains(css)) {
      scene.getStylesheets().add(css);
    }
  }

  /**
   * Updates the header information for the selected stock.
   *
   * @param symbol  the stock symbol to display
   * @param company the company name to display
   */
  public void setHeader(String symbol, String company) {
    this.titleLabel.setText(symbol);
    this.companyLabel.setText(company);
  }

  /**
   * Updates the market-statistics section.
   *
   * @param price  the formatted current price
   * @param change the formatted weekly change
   * @param high   the formatted historical high
   * @param low    the formatted historical low
   */
  public void setMarketStats(String price, String change, String high, String low) {
    this.priceLabel.setText("Current price: " + price);
    this.changeLabel.setText("Weekly change: " + change);
    this.highLabel.setText("High: " + high);
    this.lowLabel.setText("Low: " + low);

    applySignedStyle(this.changeLabel, change);
  }

  /**
   * Updates the player-position section for the selected stock.
   *
   * @param quantity     the formatted owned quantity
   * @param invested     the formatted invested value
   * @param currentValue the formatted current value
   * @param gainLoss     the formatted gain/loss value
   */
  public void setPositionStats(String quantity, String invested, String currentValue,
                               String gainLoss) {
    this.quantityLabel.setText("Owned quantity: " + quantity);
    this.investedLabel.setText("Invested value: " + invested);
    this.currentValueLabel.setText("Current value: " + currentValue);
    this.gainLossLabel.setText("Gain/Loss: " + gainLoss);

    applySignedStyle(this.gainLossLabel, gainLoss);
  }

  /**
   * Updates the ownership-state message for the selected stock.
   *
   * @param message the message to display, or blank to hide it
   */
  public void setOwnershipState(String message) {
    if (message == null || message.isBlank()) {
      this.ownershipStateLabel.setText("");
      this.ownershipStateLabel.setVisible(false);
      this.ownershipStateLabel.setManaged(false);
      return;
    }

    this.ownershipStateLabel.setText(message);
    this.ownershipStateLabel.setVisible(true);
    this.ownershipStateLabel.setManaged(true);
  }

  /**
   * Replaces the displayed price history in the chart.
   *
   * @param prices the chronological prices to render. Must not be {@code null}
   */
  public void setPriceHistory(List<BigDecimal> prices) {
    Objects.requireNonNull(prices, "Prices cannot be null");
    this.historyChart.setPrices(prices);
  }

  /**
   * Replaces the displayed recent-transaction rows.
   *
   * @param rows the rows to display. Must not be {@code null}
   */
  public void setRecentTransactions(List<String> rows) {
    Objects.requireNonNull(rows, "Rows cannot be null");
    this.recentTransactionsBox.getChildren().clear();

    for (String row : rows) {
      Label label = createLineLabel();
      label.setText(row);
      this.recentTransactionsBox.getChildren().add(label);
    }
  }

  /**
   * Registers the action to invoke when the user clicks Close.
   *
   * @param action the action to invoke. Must not be {@code null}
   */
  public void setOnClose(Runnable action) {
    Objects.requireNonNull(action, "Action cannot be null");
    this.closeButton.setOnAction(event -> action.run());
  }

  private Label createLineLabel() {
    Label label = new Label();
    label.getStyleClass().add("stock-card-line");
    label.setWrapText(true);
    return label;
  }

  private void applySignedStyle(Label label, String value) {
    label.getStyleClass().removeAll("change-positive", "change-negative");

    if (value.startsWith("+")) {
      label.getStyleClass().add("change-positive");
    } else if (value.startsWith("-")) {
      label.getStyleClass().add("change-negative");
    }
  }
}
