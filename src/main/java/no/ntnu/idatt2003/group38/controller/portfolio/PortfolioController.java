package no.ntnu.idatt2003.group38.controller.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.portfolio.PortfolioView;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;

public class PortfolioController implements Page, ModelObserver {

  private final PortfolioView view;
  private final Exchange exchange;
  private final Player player;

  private String selectedSymbol;

  public PortfolioController(Exchange exchange, Player player) {
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");

    this.view = new PortfolioView();
    this.view.setOnShareSelected(this::handleSelect);
    this.view.setOnBuySelected(this::handleBuySelected);
    this.view.setOnSellSelected(this::handleSellSelected);
  }

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

  @Override
  public void onModelChanged() {
    refresh();
  }

  private void handleSelect(Share share) {
    this.selectedSymbol = share == null ? null : share.getStock().getSymbol();
    this.view.showSelectedShare(share);
  }

  private void handleBuySelected(int quantity) {
    if (this.selectedSymbol == null || quantity <= 0) {
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

  private void refresh() {
    List<Share> actualShares = this.player.getPortfolio().getShares();
    List<Share> displayShares = aggregateShares(actualShares);

    BigDecimal portfolioValue = this.player.getPortfolio().getNetWorth();
    BigDecimal totalInvested = actualShares.stream()
        .map(share -> new PurchaseCalculator(share).calculateTotal())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalGainLoss = portfolioValue.subtract(totalInvested);
    BigDecimal totalGainLossPct = calculatePercent(totalGainLoss, totalInvested);

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

  private BigDecimal calculatePercent(BigDecimal value, BigDecimal base) {
    if (base.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return value.divide(base, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  private static final class ShareAccumulator {
    private final Share firstShare;
    private BigDecimal totalQuantity = BigDecimal.ZERO;
    private BigDecimal totalPurchaseGross = BigDecimal.ZERO;

    ShareAccumulator(Share firstShare) {
      this.firstShare = firstShare;
    }

    void add(Share share) {
      this.totalQuantity = this.totalQuantity.add(share.getQuantity());
      this.totalPurchaseGross = this.totalPurchaseGross.add(
          share.getPurchasePrice().multiply(share.getQuantity()));
    }

    Share toShare() {
      BigDecimal averagePurchasePrice = this.totalPurchaseGross.divide(
          this.totalQuantity, 10, RoundingMode.HALF_UP);
      return new Share(this.firstShare.getStock(), this.totalQuantity, averagePurchasePrice);
    }
  }
}
