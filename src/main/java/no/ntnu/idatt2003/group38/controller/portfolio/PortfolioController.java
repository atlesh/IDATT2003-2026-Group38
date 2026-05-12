package no.ntnu.idatt2003.group38.controller.portfolio;

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

    private void refresh() {
        List<Share> shares = this.player.getPortfolio().getShares();
        this.view.setShares(shares);
        this.view.setSummary(
                this.player.getMoney(),
                this.player.getPortfolio().getNetWorth(),
                this.player.getNetWorth());

        if (this.selectedShare != null && shares.contains(this.selectedShare)) {
            this.view.showSelectedShare(this.selectedShare);
        } else {
            this.selectedShare = null;
            this.view.showSelectedShare(null);
        }
    }
}
