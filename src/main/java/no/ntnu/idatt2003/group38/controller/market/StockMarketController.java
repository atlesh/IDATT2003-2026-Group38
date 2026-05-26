package no.ntnu.idatt2003.group38.controller.market;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.controller.analysis.StockAnalysisController;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.view.components.TransactionReceipt;
import no.ntnu.idatt2003.group38.view.market.StockMarketView;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.view.shell.ShellView;

/**
 * Controller for the Market page.
 *
 * <p>Connects the {@link StockMarketView} to the {@link Exchange} and {@link Player}
 * model. Listens for search input, row selection and buy clicks from the view,
 * and observes the exchange so the displayed stock list and selected stock
 * stay in sync with the model.</p>
 */
public class StockMarketController implements Page, ModelObserver {

  private final StockMarketView view;
  private final Exchange exchange;
  private final Player player;
  private final ShellView shell;

  private String searchQuery = "";
  private String selectedSymbol;

  /**
   * Creates a new market controller.
   *
   * @param exchange the exchange the user is trading on; must not be {@code null}
   * @param player   the player making purchases; must not be {@code null}
   * @param shell    the shell used to show modal dialogs and receipts; must not be {@code null}
   */
  public StockMarketController(Exchange exchange, Player player, ShellView shell) {
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");
    this.shell = Objects.requireNonNull(shell, "shell cannot be null");

    this.view = new StockMarketView();
    this.view.setOnSearch(this::handleSearch);
    this.view.setOnStockSelected(this::handleSelect);
    this.view.setOnAnalyze(this::handleAnalyze);
    this.view.setOnBuy(this::handleBuy);
  }

  // Page

  /**
   * Returns the root node of the market page.
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
   * <p>Registers the controller as an observer, attaches the stylesheet if the view
   * is part of a scene, and refreshes the displayed data.</p>
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

  // ModelObserver

  /**
   * Refreshes the page after a model update.
   */
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
    refresh();
  }

  private void handleAnalyze(Stock stock) {
    if (stock == null) {
      return;
    }

    StockAnalysisController controller =
        new StockAnalysisController(this.player, this.shell, stock);
    controller.show();
  }

  private void handleBuy(Stock stock, BigDecimal quantity) {
    if (stock == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    try {
      Transaction transaction = this.exchange.buy(stock.getSymbol(), quantity, this.player);
      showReceipt(transaction);
    } catch (IllegalArgumentException | IllegalStateException e) {
      this.view.showBuyError("Could not complete buy: " + e.getMessage());
    }
  }

  private void showReceipt(Transaction transaction) {
    TransactionReceipt receipt = new TransactionReceipt(transaction);
    receipt.setOnClose(this.shell::hideModal);
    this.shell.showModal(receipt.getRoot());
  }

  // Refresh

  /**
   * Pulls the current stock list (filtered by the search) from the
   * exchange and pushes it to the view, then re-applies the current selection
   * if the selected stock is still in the result set.
   */
  private void refresh() {
    List<Stock> stocks = this.exchange.findStocks(this.searchQuery);
    this.view.setSelectedSymbol(this.selectedSymbol);
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
