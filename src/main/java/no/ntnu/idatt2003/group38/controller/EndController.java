package no.ntnu.idatt2003.group38.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.view.EndView;
import no.ntnu.idatt2003.group38.view.StartView;

/**
 * Controller for {@link EndView}
 *
 * <p>Populates the finished-game summary and handles navigation to either a new game
 * or application exit</p>
 */
public class EndController {

    private final EndView view;
    private final Stage stage;
    private final Player player;
    private final Exchange exchange;
    private final DecimalFormat moneyFormat;

    /**
     * Creates a new end controller and wires the summary actions
     *
     * @param view the end view to control, must not be {@code null}
     * @param stage the primary stage used for scene transitions, must not be {@code null}
     * @param player the finished game's player, must not be {@code null}
     * @param exchange the finished game's exchange, must not be {@code null}
     */
    public EndController(EndView view, Stage stage, Player player, Exchange exchange) {
        this.view = Objects.requireNonNull(view, "View cannot be null");
        this.stage = Objects.requireNonNull(stage, "Stage cannot be null");
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        this.moneyFormat = new DecimalFormat("#,##0", symbols);

        this.view.setSummary(
                this.player.getName(),
                this.exchange.getWeek(),
                this.player.getPlayerStatus(),
                formatMoney(this.player.getMoney()),
                formatMoney(this.player.getPortfolio().getNetWorth()),
                formatMoney(this.player.getNetWorth()),
                this.player.getTransactionArchive().getTransactions().size()
        );

        this.view.setOnStartNewGame(this::handleStartNewGame);
        this.view.setOnExit(this::handleExit);
    }

    /**
     * Navigates to the existing new-game form
     */
    private void handleStartNewGame() {
        StartView startView = new StartView();
        new StartController(startView, this.stage);

        Scene scene = new Scene(startView.getRoot(), 760, 480);
        startView.attachTo(scene);

        this.stage.setScene(scene);
        this.stage.centerOnScreen();
    }

    /**
     * Closes the application
     */
    private void handleExit() {
        this.stage.close();
    }

    /**
     * Formats a money amount for presentation in the summary
     *
     * @param amount the amount to format
     * @return a string line like {@code "100 000"}
     */
    private String formatMoney(BigDecimal amount) {
        return this.moneyFormat.format(amount.setScale(0, RoundingMode.HALF_UP));
    }
}
