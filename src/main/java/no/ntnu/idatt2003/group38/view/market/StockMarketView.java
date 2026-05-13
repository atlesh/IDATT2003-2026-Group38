package no.ntnu.idatt2003.group38.view.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * The Market page view.
 *
 * <p>Shows a searchable list of stocks on the left in a white panel
 * and a {@link StockCard} on the right that displays details for the currently selected stock.
 */
public class StockMarketView {

  private static final String STYLESHEET = "/stylesheets/market.css";

  private final HBox root;
  private final VBox rowsContainer;
  private final StockCard stockCard;

  private Consumer<String> onSearch = query -> { };
  private Consumer<Stock> onStockSelected = stock -> { };
  private java.util.function.BiConsumer<Stock, Integer> onBuy = (stock, qty) -> { };

  /**
   * Builds the market view with an empty stock list and an empty stock card.
   */
  public StockMarketView() {
    Label title = new Label("StockMarket");
    title.getStyleClass().add("market-title");

    TextField searchField = new TextField();
    searchField.setPromptText("Search stock...");
    searchField.getStyleClass().add("market-search");
    searchField.setMaxWidth(220);
    searchField.textProperty().addListener(
        (obs, old, value) -> this.onSearch.accept(value));

    this.rowsContainer = new VBox(8);
    this.rowsContainer.getStyleClass().add("market-rows");

    HBox header = buildHeaderRow();

    ScrollPane scroll = new ScrollPane(this.rowsContainer);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("market-scroll");
    VBox.setVgrow(scroll, Priority.ALWAYS);

    VBox listPanel = new VBox(16, title, searchField, header, scroll);
    listPanel.getStyleClass().add("market-panel");
    HBox.setHgrow(listPanel, Priority.ALWAYS);

    this.stockCard = new StockCard();
    this.stockCard.setOnBuy((stock, qty) -> this.onBuy.accept(stock, qty));

    this.root = new HBox(20, listPanel, this.stockCard.getRoot());
    this.root.setPadding(new Insets(20));
    this.root.getStyleClass().add("market-view");
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
   * Replaces the rows in the table with one row per stock.
   *
   * @param stocks the stocks to display. Must not be {@code null}
   */
  public void setStocks(List<Stock> stocks) {
    Objects.requireNonNull(stocks, "stocks cannot be null");
    this.rowsContainer.getChildren().clear();
    for (Stock stock : stocks) {
      this.rowsContainer.getChildren().add(buildStockRow(stock));
    }
  }

  /**
   * Updates the stock card to show the given stock, or clears it
   * if {@code stock} is {@code null}.
   *
   * @param stock the stock to display, or {@code null} to clear the card
   */
  public void showSelectedStock(Stock stock) {
    if (stock == null) {
      this.stockCard.clear();
    } else {
      this.stockCard.show(stock);
    }
  }

  /**
   * Registers the callback to invoke when the user types in the search field.
   *
   * @param onSearch the callback; must not be {@code null}
   */
  public void setOnSearch(Consumer<String> onSearch) {
    this.onSearch = Objects.requireNonNull(onSearch, "onSearch cannot be null");
  }

  /**
   * Registers the callback to invoke when the user clicks a stock row.
   *
   * @param onStockSelected the callback. Must not be {@code null}
   */
  public void setOnStockSelected(Consumer<Stock> onStockSelected) {
    this.onStockSelected = Objects.requireNonNull(
        onStockSelected, "onStockSelected cannot be null");
  }

  /**
   * Registers the callback to invoke when the user clicks Buy on any row or
   * on the right-hand stock card.
   *
   * @param onBuy the callback. Must not be {@code null}
   */
  public void setOnBuy(java.util.function.BiConsumer<Stock, Integer> onBuy) {
    this.onBuy = Objects.requireNonNull(onBuy, "onBuy cannot be null");
  }

  // Components

  private HBox buildHeaderRow() {
    Label stockHeader = new Label("Stock");
    Label companyHeader = new Label("Company");
    Label priceHeader = new Label("Price");
    Label changeHeader = new Label("Change");
    for (Label l : List.of(stockHeader, companyHeader, priceHeader, changeHeader)) {
      l.getStyleClass().add("market-column-header");
    }

    HBox header = new HBox(
        cell(stockHeader, 90),
        cell(companyHeader, 260),
        cell(priceHeader, 90),
        cell(changeHeader, 90));
    header.getStyleClass().add("market-row-header");
    return header;
  }

  private HBox buildStockRow(Stock stock) {
    Label symbol = new Label(stock.getSymbol());
    Label company = new Label(stock.getCompany());
    Label price = new Label(formatPrice(stock.getSalesPrice()));
    Label change = new Label(formatChange(stock.getLatestPriceChangePercent()));
    applyChangeColor(change, stock.getLatestPriceChangePercent());

    HBox row = new HBox(
        cell(symbol, 90),
        cell(company, 260),
        cell(price, 90),
        cell(change, 90));
    row.getStyleClass().add("market-row");
    row.setOnMouseClicked(e -> this.onStockSelected.accept(stock));
    return row;
  }

  /**
   * Wraps a node in a fixed-width cell so columns align across rows.
   */
  private HBox cell(Node content, double width) {
    HBox box = new HBox(content);
    box.setAlignment(Pos.CENTER_LEFT);
    box.setPrefWidth(width);
    return box;
  }

  // Formatting

  private String formatPrice(BigDecimal price) {
    return price.setScale(0, RoundingMode.HALF_UP).toPlainString();
  }

  private String formatChange(BigDecimal pct) {
    String sign = pct.signum() > 0 ? "+" : "";
    return sign + pct.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
  }

  private void applyChangeColor(Label label, BigDecimal pct) {
    if (pct.signum() > 0) {
      label.getStyleClass().add("change-positive");
    } else if (pct.signum() < 0) {
      label.getStyleClass().add("change-negative");
    }
  }

  // Stylesheet
  public void attachTo(Scene scene) {
    Objects.requireNonNull(scene, "scene cannot be null");
    String css = Objects.requireNonNull(
        getClass().getResource(STYLESHEET),
        "Could not find stylesheet at " + STYLESHEET).toExternalForm();
    if (!scene.getStylesheets().contains(css)) {
      scene.getStylesheets().add(css);
    }
  }

  public void showBuyError(String message) {
    this.stockCard.showError(message);
  }
}