package no.ntnu.idatt2003.group38.controller.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.view.components.TransactionReceipt;
import no.ntnu.idatt2003.group38.view.portfolio.PortfolioView;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.view.shell.ShellView;
import no.ntnu.idatt2003.group38.controller.analysis.StockAnalysisController;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * Controller for the Portfolio page.
 *
 * <p>Connects the {@link PortfolioView} to the {@link Exchange} and {@link Player}
 * model. Aggregates owned lots into one row per symbol for display, keeps the
 * selected holding in sync with model updates, and handles quantity-based
 * buy and sell actions initiated from the view.</p>
 */
public class PortfolioController implements Page, ModelObserver {

  private final PortfolioView view;
  private final Exchange exchange;
  private final Player player;
  private final ShellView shell;

  private String selectedSymbol;

  /**
   * Creates a new portfolio controller.
   *
   * @param exchange the exchange that provides current stock prices. Must not be {@code null}
   * @param player the player whose portfolio is being displayed. Must not be {@code null}
   * @param shell the shell used to show modal receipts and error dialogs. Must not be {@code null}
   */
  public PortfolioController(Exchange exchange, Player player, ShellView shell) {
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");
    this.shell = Objects.requireNonNull(shell, "shell cannot be null");

    this.view = new PortfolioView();
    this.view.setOnShareSelected(this::handleSelect);
    this.view.setOnBuySelected(this::handleBuySelected);
    this.view.setOnSellSelected(this::handleSellSelected);
    this.view.setOnSellAll(this::handleSellAll);
    this.view.setOnAnalyzeSelected(this::handleAnalyzeSelected);
  }

  // Page

  /** {@inheritDoc} */
  @Override
  public Region getRoot() {
    return this.view.getRoot();
  }

  /** {@inheritDoc} */
  @Override
  public void onAttach() {
    this.exchange.addObserver(this);
    Scene scene = this.view.getRoot().getScene();
    if (scene != null) {
      this.view.attachTo(scene);
    }
    refresh();
  }

  /** {@inheritDoc} */
  @Override
  public void onDetach() {
    this.exchange.removeObserver(this);
  }

  // ModelObserver

  /** {@inheritDoc} */
  @Override
  public void onModelChanged() {
    refresh();
  }

  // Events

  /**
   * Updates the selected portfolio row and refreshes the detail panel.
   *
   * @param share the selected aggregated share, or {@code null} to clear the selection
   */
  private void handleSelect(Share share) {
      this.selectedSymbol = share == null ? null : share.getStock().getSymbol();
      refresh();
  }

  /**
   * Attempts to buy the requested quantity of the currently selected stock.
   *
   * @param quantity the quantity to buy
   */
  private void handleBuySelected(int quantity) {
    if (this.selectedSymbol == null || quantity <= 0) {
      return;
    }
    try {
      Transaction transaction = this.exchange.buy(
          this.selectedSymbol, BigDecimal.valueOf(quantity), this.player);
      showReceipt(transaction);
    } catch (RuntimeException e) {
      showError("Could not complete buy: " + e.getMessage());
    }
  }

  /**
   * Attempts to sell the requested quantity of the currently selected holding.
   *
   * @param quantity the quantity to sell
   */
  private void handleSellSelected(int quantity) {
    if (this.selectedSymbol == null || quantity <= 0) {
      return;
    }

    String symbol = this.selectedSymbol;
    List<Share> ownedLots = new ArrayList<>(
        this.player.getPortfolio().getShares(symbol));
    BigDecimal requestedQuantity = BigDecimal.valueOf(quantity);
    BigDecimal ownedQuantity = ownedLots.stream()
        .map(Share::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (ownedQuantity.compareTo(requestedQuantity) < 0) {
      showError("Could not complete sale: requested quantity exceeds owned quantity.");
      return;
    }

    BigDecimal unitPrice = this.exchange.getStock(symbol).getSalesPrice();
    BigDecimal remaining = requestedQuantity;

    BigDecimal totalGross = BigDecimal.ZERO;
    BigDecimal totalCommission = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    BigDecimal totalNet = BigDecimal.ZERO;
    BigDecimal totalQuantity = BigDecimal.ZERO;

    try {
      for (Share lot : ownedLots) {
        if (remaining.compareTo(BigDecimal.ZERO) == 0) break;
        BigDecimal sellQuantity = remaining.min(lot.getQuantity());
        Transaction transaction = this.exchange.sell(lot, sellQuantity, this.player);
        totalGross = totalGross.add(transaction.getCalculator().calculateGross());
        totalCommission = totalCommission.add(transaction.getCalculator().calculateCommission());
        totalTax = totalTax.add(transaction.getCalculator().calculateTax());
        totalNet = totalNet.add(transaction.getCalculator().calculateTotal());
        totalQuantity = totalQuantity.add(sellQuantity);
        remaining = remaining.subtract(sellQuantity);
      }
    } catch (RuntimeException e) {
      showError("Could not complete sale: " + e.getMessage());
      return;
    }

    if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
      showReceipt("Sold", symbol, totalQuantity, unitPrice,
          totalGross, totalCommission, totalTax, totalNet);
    }
  }

  /**
   * Sells every owned share and shows one grouped receipt per stock symbol.
   */
  private void handleSellAll() {
    if (this.player.getPortfolio().getShares().isEmpty()) {
      return;
    }

    List<Transaction> transactions;
    try {
      transactions = this.exchange.sellAll(this.player);
    } catch (RuntimeException e) {
      showError("Could not sell all holdings: " + e.getMessage());
      return;
    }

    Map<String, BigDecimal> unitPriceBySymbol = new LinkedHashMap<>();
    Map<String, BigDecimal> grossBySymbol = new LinkedHashMap<>();
    Map<String, BigDecimal> commissionBySymbol = new LinkedHashMap<>();
    Map<String, BigDecimal> taxBySymbol = new LinkedHashMap<>();
    Map<String, BigDecimal> netBySymbol = new LinkedHashMap<>();
    Map<String, BigDecimal> quantityBySymbol = new LinkedHashMap<>();

    try {
      for (Transaction transaction : transactions) {
        String symbol = transaction.getShare().getStock().getSymbol();
        unitPriceBySymbol.putIfAbsent(symbol, this.exchange.getStock(symbol).getSalesPrice());

        grossBySymbol.merge(symbol, transaction.getCalculator().calculateGross(), BigDecimal::add);
        commissionBySymbol.merge(symbol,
                transaction.getCalculator().calculateCommission(), BigDecimal::add);
        taxBySymbol.merge(symbol, transaction.getCalculator().calculateTax(), BigDecimal::add);
        netBySymbol.merge(symbol, transaction.getCalculator().calculateTotal(), BigDecimal::add);
        quantityBySymbol.merge(symbol, transaction.getShare().getQuantity(), BigDecimal::add);
      }
    } catch (RuntimeException e) {
      showError("Could not build sell-all receipt: " + e.getMessage());
      return;
    }

    for (String symbol : quantityBySymbol.keySet()) {
      showReceipt(
          "Sold",
          symbol,
          quantityBySymbol.get(symbol),
          unitPriceBySymbol.get(symbol),
          grossBySymbol.get(symbol),
          commissionBySymbol.get(symbol),
          taxBySymbol.get(symbol),
          netBySymbol.get(symbol));
    }
  }

  /**
   * Opens the stock-analysis modal for the currently selected holding.
   */
  private void handleAnalyzeSelected() {
    if (this.selectedSymbol == null) {
      return;
    }

    Stock stock = this.exchange.getStock(this.selectedSymbol);
    StockAnalysisController controller =
        new StockAnalysisController(this.player, this.shell, stock);
    controller.show();
  }

  // Refresh

  /**
   * Pulls the current holdings from the player's portfolio, aggregates them
   * into one display row per symbol, recalculates summary metrics and re-applies
   * the current selection if the selected holding still exists.
   */
  private void refresh() {
    List<Share> actualShares = this.player.getPortfolio().getShares();
    List<Share> displayShares = aggregateShares(actualShares);

    BigDecimal portfolioValue = this.player.getPortfolio().getNetWorth();
    BigDecimal totalInvested = actualShares.stream()
        .map(share -> new PurchaseCalculator(share).calculateTotal())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalGainLoss = portfolioValue.subtract(totalInvested);
    BigDecimal totalGainLossPct = calculatePercent(totalGainLoss, totalInvested);

    this.view.setSelectedSymbol(this.selectedSymbol);
    this.view.setShares(displayShares);
    this.view.setSummary(
        this.player.getMoney(),
        portfolioValue,
        this.player.getNetWorth(),
        totalGainLoss,
        totalGainLossPct);

    Share selectedShare = findDisplayShare(displayShares);
    if (selectedShare != null) {
      this.view.showSelectedShare(selectedShare);
    } else {
      this.selectedSymbol = null;
      this.view.showSelectedShare(null);
    }
    this.view.setHasHoldings(!actualShares.isEmpty());
  }

  /**
   * Shows a receipt for one committed transaction.
   *
   * @param transaction the committed transaction to present
   */
  private void showReceipt(Transaction transaction) {
    TransactionReceipt receipt = new TransactionReceipt(transaction);
    receipt.setOnClose(this.shell::hideModal);
    this.shell.showModal(receipt.getRoot());
  }

  /**
   * Shows a grouped receipt for one completed sell action.
   *
   * @param action the action label to display
   * @param symbol the stock symbol
   * @param quantity the total quantity sold
   * @param unitPrice the current unit price
   * @param gross the gross sale value
   * @param commission the total commission
   * @param tax the total tax
   * @param total the net amount received
   */
  private void showReceipt(String action, String symbol, BigDecimal quantity,
                           BigDecimal unitPrice, BigDecimal gross, BigDecimal commission,
                           BigDecimal tax, BigDecimal total) {
    TransactionReceipt receipt = new TransactionReceipt(
        action, symbol, quantity, unitPrice, gross, commission, tax, total);
    receipt.setOnClose(this.shell::hideModal);
    this.shell.showModal(receipt.getRoot());
  }

  /**
   * Aggregates all owned lots into one display share per symbol using a
   * weighted average purchase price.
   *
   * @param shares the raw portfolio lots to aggregate
   * @return one display share per symbol, preserving the original symbol order
   */
  private List<Share> aggregateShares(List<Share> shares) {
    Map<String, ShareAccumulator> groups = new LinkedHashMap<>();

    for (Share share : shares) {
      String symbol = share.getStock().getSymbol();
      ShareAccumulator accumulator = groups.computeIfAbsent(
          symbol, ignored -> new ShareAccumulator(share));
      accumulator.add(share);
    }

    return groups.values().stream()
        .map(ShareAccumulator::toShare)
        .toList();
  }

  /**
   * Returns the currently selected display share, or {@code null} if no symbol
   * is selected or the selected symbol is no longer present in the display list.
   *
   * @param shares the currently displayed aggregated holdings
   * @return the selected display share, or {@code null}
   */
  private Share findDisplayShare(List<Share> shares) {
    if (this.selectedSymbol == null) {
      return null;
    }

    for (Share share : shares) {
      if (share.getStock().getSymbol().equals(this.selectedSymbol)) {
        return share;
      }
    }
    return null;
  }

  // Formatting

  /**
   * Returns {@code value} as a percentage of {@code base}, or zero if
   * {@code base} is zero.
   *
   * @param value the value to compare against the base
   * @param base the base value
   * @return the percentage representation of {@code value} relative to {@code base}
   */
  private BigDecimal calculatePercent(BigDecimal value, BigDecimal base) {
    if (base.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return value.divide(base, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Shows a simple error alert to the user.
   *
   * @param message the message to display
   */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Helper that accumulates lots for one stock symbol before they are converted
   * into a single display share.
   */
  private static final class ShareAccumulator {
    private final Share firstShare;
    private BigDecimal totalQuantity = BigDecimal.ZERO;
    private BigDecimal totalPurchaseGross = BigDecimal.ZERO;

    /**
     * Creates a new accumulator anchored to the first encountered lot for a symbol.
     *
     * @param firstShare the first lot seen for the symbol
     */
    ShareAccumulator(Share firstShare) {
      this.firstShare = firstShare;
    }

    /**
     * Adds one owned lot into the accumulated quantity and purchase gross.
     *
     * @param share the lot to add
     */
    void add(Share share) {
      this.totalQuantity = this.totalQuantity.add(share.getQuantity());
      this.totalPurchaseGross = this.totalPurchaseGross.add(
          share.getPurchasePrice().multiply(share.getQuantity()));
    }

    /**
     * Converts the accumulated data into one aggregated display share.
     *
     * @return a share containing the total quantity and weighted average purchase price
     */
    Share toShare() {
      BigDecimal averagePurchasePrice = this.totalPurchaseGross.divide(
          this.totalQuantity, 10, RoundingMode.HALF_UP);
      return new Share(this.firstShare.getStock(), this.totalQuantity, averagePurchasePrice);
    }
  }
}
