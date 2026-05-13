package no.ntnu.idatt2003.group38.controller.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
    private Share selectedShare;

    public PortfolioController(Exchange exchange, Player player) {
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
        this.player = Objects.requireNonNull(player, "Player cannot be null");

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
        this.selectedShare = share;
        this.view.showSelectedShare(share);
    }

    private void handleBuySelected() {
        if (this.selectedShare == null) {
            return;
        }

        try {
            this.exchange.buy(
                    this.selectedShare.getStock().getSymbol(),
                    BigDecimal.ONE,
                    this.player
            );
        } catch (RuntimeException e) {
            System.err.println("Buy failed: "  + e.getMessage());
        }
    }

    private void handleSellSelected() {
        if (this.selectedShare == null) {
            return;
        }

        try {
            this.exchange.sell(this.selectedShare, this.player);
        } catch (RuntimeException e) {
            System.err.println("Sell failed: "  + e.getMessage());
        }
    }

    private void refresh() {
        List<Share> shares = this.player.getPortfolio().getShares();

        BigDecimal portfolioValue = this.player.getPortfolio().getNetWorth();
        BigDecimal totalInvested = shares.stream()
                .map(share -> new PurchaseCalculator(share).calculateTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGainLoss = portfolioValue.subtract(totalInvested);
        BigDecimal totalGainLossPct = calculatePercent(totalGainLoss, totalInvested);

        this.view.setShares(shares);
        this.view.setSummary(
                this.player.getMoney(),
                portfolioValue,
                this.player.getNetWorth(),
                totalGainLoss,
                totalGainLossPct);

        if (this.selectedShare != null && shares.contains(this.selectedShare)) {
            this.view.showSelectedShare(this.selectedShare);
        } else {
            this.selectedShare = null;
            this.view.showSelectedShare(null);
        }
    }

    private BigDecimal calculatePercent(BigDecimal.value, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(base, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}
