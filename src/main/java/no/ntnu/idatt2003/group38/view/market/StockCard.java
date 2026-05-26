package no.ntnu.idatt2003.group38.view.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.view.components.PriceSparkline;

/**
 * The selected stock card on the Market page.
 *
 * <p>Shows information about the currently selected {@link Stock} and a Buy
 * button that lets the user purchase shares. When no stock is selected the
 * card shows an empty-state message instead.
 */
public class StockCard {

  private static final Pattern QUANTITY_PATTERN = Pattern.compile("\\d*(\\.\\d*)?");

  private final VBox root;
  private final Label symbolLabel;
  private final Label companyLabel;
  private final Label priceLabel;
  private final Label changeLabel;
  private final Button buyButton;
  private final Label highLabel;
  private final Label lowLabel;
  private final PriceSparkline sparkline;
  private final Label quantityHeadingLabel;
  private final TextField quantityField;
  private final Label grossLabel;
  private final Label commissionLabel;
  private final Label taxLabel;
  private final Label totalLabel;
  private final Label errorLabel;
  private final Button analyzeButton;
  private final HBox actionRow;

  private Stock currentStock;
  private BiConsumer<Stock, BigDecimal> onBuy = (stock, qty) -> { };
  private Consumer<Stock> onAnalyze = stock -> { };

  /**
   * Builds an empty stock card.
   */
  public StockCard() {
    Label titleLabel = new Label("Selected stock");
    titleLabel.getStyleClass().add("stock-card-title");

    this.symbolLabel = new Label();
    this.symbolLabel.getStyleClass().add("stock-card-symbol");

    this.companyLabel = new Label();
    this.companyLabel.getStyleClass().add("stock-card-company");

    this.priceLabel = new Label();
    this.priceLabel.getStyleClass().add("stock-card-line");

    this.changeLabel = new Label();
    this.changeLabel.getStyleClass().add("stock-card-line");

    this.highLabel = new Label();
    this.highLabel.getStyleClass().add("stock-card-line");

    this.lowLabel = new Label();
    this.lowLabel.getStyleClass().add("stock-card-line");

    this.sparkline = new PriceSparkline();

    this.quantityHeadingLabel = new Label("Quantity");
    this.quantityHeadingLabel.getStyleClass().add("stock-card-line");

    this.quantityField = new TextField("1");
    this.quantityField.getStyleClass().add("quantity-input");
    this.quantityField.setTextFormatter(new TextFormatter<>(change ->
        QUANTITY_PATTERN.matcher(change.getControlNewText()).matches() ? change : null));
    this.quantityField.textProperty().addListener((obs, oldV, newV) -> {
      updateCost();
      updateBuyButtonState();
      clearError();
    });

    this.grossLabel = new Label();
    this.grossLabel.getStyleClass().add("stock-card-line");
    this.commissionLabel = new Label();
    this.commissionLabel.getStyleClass().add("stock-card-line");
    this.taxLabel = new Label();
    this.taxLabel.getStyleClass().add("stock-card-line");
    this.totalLabel = new Label();
    this.totalLabel.getStyleClass().add("stock-card-line");

    this.buyButton = new Button("Buy");
    this.buyButton.getStyleClass().add("buy-button");
    this.buyButton.setOnAction(e -> {
      BigDecimal quantity = parseQuantity();
      if (this.currentStock != null && quantity != null) {
        this.onBuy.accept(this.currentStock, quantity);
      }
    });

    this.analyzeButton = new Button("Analyze");
    this.analyzeButton.getStyleClass().add("buy-button");
    this.analyzeButton.setOnAction(e -> {
      if (this.currentStock != null) {
        this.onAnalyze.accept(this.currentStock);
      }
    });

    this.actionRow = new HBox(8, this.analyzeButton, this.buyButton);
    this.actionRow.setAlignment(Pos.CENTER);

    this.errorLabel = new Label();
    this.errorLabel.setStyle("-fx-text-fill: red;");

    this.root = new VBox(8,
        titleLabel,
        this.symbolLabel,
        this.companyLabel,
        this.priceLabel,
        this.changeLabel,
        this.highLabel,
        this.lowLabel,
        this.sparkline.getRoot(),
        this.quantityHeadingLabel,
        this.quantityField,
        this.grossLabel,
        this.commissionLabel,
        this.taxLabel,
        this.totalLabel,
        this.actionRow,
        this.errorLabel);
    this.root.setAlignment(Pos.TOP_CENTER);
    this.root.getStyleClass().addAll("market-panel", "stock-card");

    clear();
  }

  /**
   * Returns the root node so the page can mount the card.
   *
   * @return the root layout container of the card
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Populates the card with the given stock.
   *
   * @param stock the stock to display; must not be {@code null}
   */
  public void show(Stock stock) {
    this.currentStock = stock;

    BigDecimal pct = stock.getLatestPriceChangePercent();

    this.symbolLabel.setText(stock.getSymbol());
    this.companyLabel.setText(stock.getCompany());
    this.priceLabel.setText("Price: " + formatPrice(stock.getSalesPrice()));
    this.changeLabel.setText("Change: " + formatChange(pct));
    this.highLabel.setText("High: " + formatPrice(stock.getHighestPrice()));
    this.lowLabel.setText("Low: " + formatPrice(stock.getLowestPrice()));
    this.sparkline.setPrices(stock.getHistoricalPrices());
    applyChangeColor(pct);

    this.quantityField.setText("1");
    updateCost();
    updateBuyButtonState();

    setDetailsVisible(true);

    clearError();
  }

  /**
   * Clears the card and shows the empty state.
   */
  public void clear() {
    this.currentStock = null;
    this.symbolLabel.setText("No stock selected");
    this.quantityField.setText("1");
    setDetailsVisible(false);
    updateCost();
    updateBuyButtonState();
    clearError();
  }

  /**
   * Registers the action to invoke when the user clicks Buy.
   *
   * @param onBuy the action to invoke with the currently selected stock;
   *              must not be {@code null}
   */
  public void setOnBuy(BiConsumer<Stock, BigDecimal> onBuy) {
    this.onBuy = Objects.requireNonNull(onBuy, "onBuy cannot be null");
  }

  /**
   * Registers the action to invoke when the user clicks Analyze.
   *
   * @param onAnalyze the action to invoke with the currently selected stock;
   *                  must not be {@code null}
   */
  public void setOnAnalyze(Consumer<Stock> onAnalyze) {
    this.onAnalyze = Objects.requireNonNull(onAnalyze, "onAnalyze cannot be null");
  }

  // Helpers

  private void setDetailsVisible(boolean visible) {
    for (var node : new javafx.scene.Node[] {
        this.companyLabel, this.priceLabel, this.changeLabel,
        this.highLabel, this.lowLabel,
        this.sparkline.getRoot(),
        this.quantityHeadingLabel, this.quantityField, this.grossLabel, this.commissionLabel,
        this.taxLabel, this.totalLabel, this.actionRow }) {
      node.setVisible(visible);
      node.setManaged(visible);
    }
  }

  private void updateCost() {
    if (this.currentStock == null) {
      setPreviewPlaceholder("");
      return;
    }

    BigDecimal quantity = parseQuantity();
    if (quantity == null) {
      setPreviewPlaceholder("-");
      return;
    }

    Share preview = new Share(this.currentStock, quantity, this.currentStock.getSalesPrice());
    PurchaseCalculator calc = new PurchaseCalculator(preview);

    this.grossLabel.setText("Gross: " + formatAmount(calc.calculateGross()));
    this.commissionLabel.setText("Commission: " + formatAmount(calc.calculateCommission()));
    this.taxLabel.setText("Tax: " + formatAmount(calc.calculateTax()));
    this.totalLabel.setText("Total: " + formatAmount(calc.calculateTotal()));
  }

  private BigDecimal parseQuantity() {
    String raw = this.quantityField.getText();
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

  private void setPreviewPlaceholder(String value) {
    this.grossLabel.setText(value.isEmpty() ? "" : "Gross: " + value);
    this.commissionLabel.setText(value.isEmpty() ? "" : "Commission: " + value);
    this.taxLabel.setText(value.isEmpty() ? "" : "Tax: " + value);
    this.totalLabel.setText(value.isEmpty() ? "" : "Total: " + value);
  }

  private void updateBuyButtonState() {
    this.buyButton.setDisable(this.currentStock == null || parseQuantity() == null);
  }

  // Formatting

  private String formatPrice(BigDecimal value) {
    return value.setScale(0, RoundingMode.HALF_UP).toPlainString();
  }

  private String formatAmount(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private String formatChange(BigDecimal pct) {
    String sign = pct.signum() > 0 ? "+" : "";
    return sign + pct.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
  }

  private void applyChangeColor(BigDecimal pct) {
    this.changeLabel.getStyleClass().removeAll("change-positive", "change-negative");
    if (pct.signum() > 0) {
      this.changeLabel.getStyleClass().add("change-positive");
    } else if (pct.signum() < 0) {
      this.changeLabel.getStyleClass().add("change-negative");
    }
  }

  /**
   * Shows an error message below the action buttons.
   *
   * @param message the message to display
   */
  public void showError(String message) {
    this.errorLabel.setText(message);
    this.errorLabel.setVisible(true);
    this.errorLabel.setManaged(true);
  }

  /**
   * Clears any currently visible error message.
   */
  public void clearError() {
    this.errorLabel.setText("");
    this.errorLabel.setVisible(false);
    this.errorLabel.setManaged(false);
  }
}
