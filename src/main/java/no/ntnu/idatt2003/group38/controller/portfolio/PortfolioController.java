package no.ntnu.idatt2003.group38.controller.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.portfolio.PortfolioView;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;

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
  private final DecimalFormat moneyFormat;

  private String selectedSymbol;

  /**
   * Creates a new portfolio controller.
   *
   * @param exchange the exchange that provides current stock prices. Must not be {@code null}
   * @param player the player whose portfolio is being displayed. Must not be {@code null}
   */
  public PortfolioController(Exchange exchange, Player player) {
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
    symbols.setGroupingSeparator(' ');
    this.moneyFormat = new DecimalFormat("#,##0", symbols);

    this.view = new PortfolioView();
    this.view.setOnShareSelected(this::handleSelect);
    this.view.setOnBuySelected(this::handleBuySelected);
    this.view.setOnSellSelected(this::handleSellSelected);
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

  private void handleSelect(Share share) {
      this.selectedSymbol = share == null ? null : share.getStock().getSymbol();
      refresh();
  }

  private void handleBuySelected(int quantity) {
    if (this.selectedSymbol == null || quantity <= 0) {
      return;
    }
    if (!confirmTrade(
        "Confirm buy",
        "Buy " + quantity + " " + this.selectedSymbol + " share(s)?",
        buildBuyReceipt(quantity))) {
        return;
    }

    try {
      this.exchange.buy(this.selectedSymbol, BigDecimal.valueOf(quantity), this.player);
    } catch (RuntimeException e) {
      System.err.println("Buy failed: " + e.getMessage());
    }
  }

  private void handleSellSelected(int quantity) {
      if (this.selectedSymbol == null || quantity <= 0) {
          return;
      }
      if (!confirmTrade(
          "Confirm sell",
          "Sell " + quantity + " " + this.selectedSymbol + " share(s)?",
          buildSellReceipt(quantity))) {
          return;
      }

      BigDecimal remaining = BigDecimal.valueOf(quantity);
      List<Share> ownedLots = new ArrayList<>(this.player.getPortfolio().getShares(selectedSymbol));

      try {
          for (Share lot : ownedLots) {
              if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                  break;
              }
              BigDecimal sellQuantity = remaining.min(lot.getQuantity());
              this.exchange.sell(lot, sellQuantity, this.player);
              remaining = remaining.subtract(sellQuantity);
          }
      } catch (RuntimeException e) {
          System.err.println("Sell failed: " + e.getMessage());
      }
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

  /**
   * Shows a confirmation dialog for a trade action.
   *
   * @param title the dialog window title
   * @param header the confirmation question shown as dialog header
   * @param details the detailed transaction receipt shown in the content area
   * @return {@code true} if the user confirms with OK, {@code false} otherwise
   */
  private boolean confirmTrade(String title, String header, String details) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle(title);
      alert.setHeaderText(header);

      Label detailsLabel = new Label(details);
      detailsLabel.setWrapText(true);
      detailsLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 14px;");

      alert.getDialogPane().setContent(detailsLabel);
      alert.getDialogPane().setPrefWidth(420);

      if (this.view.getRoot().getScene() != null) {
          alert.initOwner(this.view.getRoot().getScene().getWindow());
      }

      return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }

  /**
   * Builds the receipt text shown before a buy is committed.
   *
   * @param quantity the quantity the user wants to buy
   * @return a multi-line receipt preview for the buy action
   */
  private String buildBuyReceipt(int quantity) {
    Stock stock = this.exchange.getStock(this.selectedSymbol);
    Share previewShare = new Share(stock, BigDecimal.valueOf(quantity), stock.getSalesPrice());
    PurchaseCalculator calculator = new PurchaseCalculator(previewShare);

    return String.join("\n",
        "Price per share: " + formatMoney(stock.getSalesPrice()),
        "Commission: " + formatMoney(calculator.calculateCommission()),
        "Total cost: " + formatMoney(calculator.calculateTotal()));
  }

  /**
   * Builds the receipt text shown before a sell is committed.
   *
   * <p>The preview follows the same FIFO lot traversal used by the actual sell
   * logic so commission, tax and proceeds match the transaction that will be
   * executed.</p>
   *
   * @param quantity the quantity the user wants to sell
   * @return a multi-line receipt preview for the sell action
   */
  private String buildSellReceipt(int quantity) {
    BigDecimal requestedQuantity = BigDecimal.valueOf(quantity);
    BigDecimal currentPrice = this.exchange.getStock(this.selectedSymbol).getSalesPrice();
    BigDecimal remaining = requestedQuantity;
    BigDecimal commission = BigDecimal.ZERO;
    BigDecimal tax = BigDecimal.ZERO;
    BigDecimal totalProceeds = BigDecimal.ZERO;

    List<Share> ownedLots = new ArrayList<>(this.player.getPortfolio().getShares(this.selectedSymbol));
    for (Share lot : ownedLots) {
      if (remaining.compareTo(BigDecimal.ZERO) == 0) {
        break;
      }

      BigDecimal sellQuantity = remaining.min(lot.getQuantity());
      Share soldPart = new Share(lot.getStock(), sellQuantity, lot.getPurchasePrice());
      SaleCalculator calculator = new SaleCalculator(soldPart);

      commission = commission.add(calculator.calculateCommission());
      tax = tax.add(calculator.calculateTax());
      totalProceeds = totalProceeds.add(calculator.calculateTotal());
      remaining = remaining.subtract(sellQuantity);
    }

    return String.join("\n",
        "Price per share: " + formatMoney(currentPrice),
        "Commission: " + formatMoney(commission),
        "Tax: " + formatMoney(tax),
        "Net proceeds: " + formatMoney(totalProceeds));
  }

  // Formatting

  private String formatMoney(BigDecimal value) {
    return this.moneyFormat.format(value.setScale(0, RoundingMode.HALF_UP));
  }

  /**
   * Returns {@code value} as a percentage of {@code base}, or zero if
   * {@code base} is zero.
   */
  private BigDecimal calculatePercent(BigDecimal value, BigDecimal base) {
    if (base.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return value.divide(base, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
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
