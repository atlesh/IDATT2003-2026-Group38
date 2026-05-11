package no.ntnu.idatt2003.group38.controller.market;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.market.StockMarketView;
import no.ntnu.idatt2003.group38.view.shell.Page;

/**
 * Controller for the Market page.
 *
 * <p>Connects the {@link StockMarketView} to the {@link Exchange} and {@link Player}
 * model. Listens for search input, row selection and Buy clicks from the view,
 * and observes the exchange so the displayed stock list and selected stock
 * stay in sync with the model.
 */
public class StockMarketController implements Page, ModelObserver {

  private final StockMarketView view;
  private final Exchange exchange;
  private final Player player;

  private String searchQuery = "";
  private String selectedSymbol;

  /**
   * Creates a new market controller.
   *
   * @param exchange the exchange the user is trading on. Must not be {@code null}
   * @param player the player making purchases. Must not be {@code null}
   */
  public StockMarketController(Exchange exchange, Player player) {
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");

    this.view = new StockMarketView();
    this.view.setOnSearch(this::handleSearch);
    this.view.setOnStockSelected(this::handleSelect);
    this.view.setOnBuy(this::handleBuy);
  }

  // Page

  @Override
  public Region getRoot() {
    return this.view.getRoot();
  }

  @Override
  public void onAttach() {
    this.exchange.addObserver(this);
    Scene scene = this.view.getRoot().getScene();
    if (scene != null) {
      this.view.attachTo(scene);
    }
    refresh();
  }

  @Override
  public void onDetach() {
    this.exchange.removeObserver(this);
  }

  // ModelObserver

  @Override
  public void onModelChanged() {
    refresh();
  }

  // Events

  private void handleSearch(String query) {
    this.searchQuery = query == null ? "" : query;
    refresh();
  }

  private void handleSelect(Stock stock) {
    this.selectedSymbol = stock == null ? null : stock.getSymbol();
    this.view.showSelectedStock(stock);
  }

  private void handleBuy(Stock stock, int quantity) {
    if (stock == null) {
      return;
    }
    try {
      this.exchange.buy(stock.getSymbol(), BigDecimal.valueOf(quantity), this.player);
    } catch (RuntimeException e) {
      System.err.println("Buy failed: " + e.getMessage());
    }
  }

  // Refresh

  /**
   * Pulls the current stock list (filtered by the search) from the
   * exchange and pushes it to the view, then re-applies the current selection
   * if the selected stock is still in the result set.
   */
  private void refresh() {
    List<Stock> stocks = this.exchange.findStocks(this.searchQuery);
    this.view.setStocks(stocks);

    Stock selected = findSelected(stocks);
    this.view.showSelectedStock(selected);
    if (selected == null) {
      this.selectedSymbol = null;
    }
  }

  /**
   * Returns the stock matching the current selected symbol, or {@code null}
   * if no symbol is selected or the previously selected symbol is no longer
   * in the result set.
   */
  private Stock findSelected(List<Stock> stocks) {
    if (this.selectedSymbol == null) {
      return null;
    }
    for (Stock stock : stocks) {
      if (stock.getSymbol().equals(this.selectedSymbol)) {
        return stock;
      }
    }
    return null;
  }
}