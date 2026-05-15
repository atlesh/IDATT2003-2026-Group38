package no.ntnu.idatt2003.group38.view.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * A titled, read-only list of stocks.
 *
 * <p>Shows a heading followed by a column header row and one row per stock
 * (symbol, company, price, weekly change).
 */
public class StatPanel {

  private static final double SYMBOL_WIDTH = 80;
  private static final double COMPANY_WIDTH = 200;
  private static final double PRICE_WIDTH = 80;
  private static final double CHANGE_WIDTH = 80;

  private final VBox root;
  private final VBox rowsContainer;
  private final Label emptyLabel;

  /**
   * Builds an empty stat panel with the given heading.
   *
   * @param title the heading shown at the top of the panel. Must not be {@code null}
   */
  public StatPanel(String title) {
    Objects.requireNonNull(title, "title cannot be null");

    Label heading = new Label(title);
    heading.getStyleClass().add("stat-panel-title");

    HBox header = buildHeaderRow();

    this.rowsContainer = new VBox(6);
    this.rowsContainer.getStyleClass().add("stat-panel-rows");

    this.emptyLabel = new Label("No data yet");
    this.emptyLabel.getStyleClass().add("stat-panel-empty");

    ScrollPane scroll = new ScrollPane(this.rowsContainer);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("stat-panel-scroll");
    VBox.setVgrow(scroll, Priority.ALWAYS);

    this.root = new VBox(12, heading, header, scroll);
    this.root.getStyleClass().add("stat-panel");
    HBox.setHgrow(this.root, Priority.ALWAYS);
  }

  /**
   * Returns the root node so the panel can be mounted in a layout.
   *
   * @return the root layout container of the panel
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Replaces the rows with one row per stock. An empty list shows a
   * placeholder message instead.
   *
   * @param stocks the stocks to display. Must not be {@code null}
   */
  public void setStocks(List<Stock> stocks) {
    Objects.requireNonNull(stocks, "stocks cannot be null");
    this.rowsContainer.getChildren().clear();

    if (stocks.isEmpty()) {
      this.rowsContainer.getChildren().add(this.emptyLabel);
      return;
    }

    for (Stock stock : stocks) {
      this.rowsContainer.getChildren().add(buildStockRow(stock));
    }
  }

  //  Component construction

  private HBox buildHeaderRow() {
    Label symbolHeader = new Label("Stock");
    Label companyHeader = new Label("Company");
    Label priceHeader = new Label("Price");
    Label changeHeader = new Label("Change");
    for (Label l : List.of(symbolHeader, companyHeader, priceHeader, changeHeader)) {
      l.getStyleClass().add("stat-panel-column-header");
    }

    HBox header = new HBox(
        cell(symbolHeader, SYMBOL_WIDTH),
        cell(companyHeader, COMPANY_WIDTH),
        cell(priceHeader, PRICE_WIDTH),
        cell(changeHeader, CHANGE_WIDTH));
    header.getStyleClass().add("stat-panel-row-header");
    return header;
  }

  private HBox buildStockRow(Stock stock) {
    Label symbol = new Label(stock.getSymbol());
    Label company = new Label(stock.getCompany());
    Label price = new Label(formatPrice(stock.getSalesPrice()));
    Label change = new Label(formatChange(stock.getLatestPriceChangePercent()));
    applyChangeColor(change, stock.getLatestPriceChangePercent());

    HBox row = new HBox(
        cell(symbol, SYMBOL_WIDTH),
        cell(company, COMPANY_WIDTH),
        cell(price, PRICE_WIDTH),
        cell(change, CHANGE_WIDTH));
    row.getStyleClass().add("stat-panel-row");
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
}