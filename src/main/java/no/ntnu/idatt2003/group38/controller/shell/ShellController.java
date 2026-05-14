package no.ntnu.idatt2003.group38.controller.shell;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import no.ntnu.idatt2003.group38.controller.market.StockMarketController;
import no.ntnu.idatt2003.group38.controller.portfolio.PortfolioController;
import no.ntnu.idatt2003.group38.controller.transaction.TransactionHistoryController;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.view.shell.ShellView;
import no.ntnu.idatt2003.group38.view.shell.SideNav.Destination;

/**
 * Owns the running game and orchestrates the application shell.
 *
 * <p>The controller is responsible for:
 * <ul>
 *   <li>Holding the {@link Player} and {@link Exchange} for the current game,</li>
 *   <li>Observing the model and pushing fresh values into the top bar.</li>
 *   <li>Routing side-nav clicks to the right {@link Page}.</li>
 *   <li>Managing page lifecycle.</li>
 * </ul>
 */
public class ShellController implements ModelObserver {

  private final ShellView shell;
  private final Stage stage;
  private final Player player;
  private final Exchange exchange;

  private final DecimalFormat moneyFormat;

  private Page currentPage;

  /**
   * Creates a new shell controller, wires the side nav, observes the model and
   * mounts the initial page.
   *
   * @param shell the shell view to control. Must not be {@code null}
   * @param stage the primary stage. Must not be {@code null}
   * @param player the player for this game. Must not be {@code null}
   * @param exchange the exchange for this game. Must not be {@code null}
   */
  public ShellController(ShellView shell, Stage stage, Player player, Exchange exchange) {
    this.shell = Objects.requireNonNull(shell, "shell cannot be null");
    this.stage = Objects.requireNonNull(stage, "stage cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
    symbols.setGroupingSeparator(' ');
    this.moneyFormat = new DecimalFormat("#,##0", symbols);

    this.shell.getSideNav().setOnNavigate(this::navigateTo);
    this.exchange.addObserver(this);

    refreshTopBar();
    navigateTo(Destination.DASHBOARD);
  }

  // Navigation

  /**
   * Navigates to the given destination, detaching the current page and
   * attaching the new one.
   *
   * @param destination the destination. Must not be {@code null}
   */
  public void navigateTo(Destination destination) {
    Objects.requireNonNull(destination, "destination cannot be null");

    if (this.currentPage != null) {
      this.currentPage.onDetach();
    }

    Page page = createPage(destination);
    this.currentPage = page;

    this.shell.setContent(page.getRoot());
    this.shell.getSideNav().setActive(destination);

    page.onAttach();
  }

  /**
   * Builds the {@link Page} for the given destination.
   *
   * @param destination the destination to build a page for
   * @return the page that should be mounted
   */
  private Page createPage(Destination destination) {
    return switch (destination) {
      case DASHBOARD -> new PlaceholderPage("Dashboard");
      case MARKET -> new StockMarketController(this.exchange, this.player, this.shell);
      case PORTFOLIO -> new PortfolioController(this.exchange, this.player);
      case TRANSACTIONS -> new TransactionHistoryController(this.exchange, this.player);
      case WEEKLY_GAINERS -> new PlaceholderPage("Weekly Gainers");
      case WEEKLY_LOSERS -> new PlaceholderPage("Weekly Losers");
    };
  }

  // Observer

  /**
   * Called by observable models the shell subscribes to
   * whenever its state changes. Refreshes the top bar so it's synced with the model.
   */
  @Override
  public void onModelChanged() {
    refreshTopBar();
  }

  /**
   * Pushes fresh values from the model into the top bar.
   */
  private void refreshTopBar() {
    this.shell.getTopBar().setWeek(this.exchange.getWeek());
    this.shell.getTopBar().setCash(formatMoney(this.player.getMoney()));
    this.shell.getTopBar().setNetWorth(formatMoney(this.player.getNetWorth()));
    this.shell.getTopBar().setStatus(this.player.getPlayerStatus());
  }

  /**
   * Formats the amount for the top bar.
   *
   * @param amount the amount to format
   * @return a string like {@code "100 000"}
   */
  private String formatMoney(BigDecimal amount) {
    return this.moneyFormat.format(amount.setScale(0, RoundingMode.HALF_UP));
  }

  // Lifecycle

  /**
   * Releases all observers held by controller.
   * Called when the player exits the running game.
   */
  public void dispose() {
    if (this.currentPage != null) {
      this.currentPage.onDetach();
      this.currentPage = null;
    }
    this.exchange.removeObserver(this);
  }

  public Stage getStage() {
    return stage;
  }


  private static final class PlaceholderPage implements Page {
    private final Region root;

    PlaceholderPage(String name) {
      Label label = new Label(name + " — coming soon");
      label.setStyle("-fx-font-family: 'Roboto'; -fx-font-size: 20px; -fx-text-fill: white;");
      StackPane pane = new StackPane(label);
      pane.getStyleClass().add("shell-content");
      this.root = pane;
    }

    @Override
    public Region getRoot() {
      return this.root;
    }
  }
}