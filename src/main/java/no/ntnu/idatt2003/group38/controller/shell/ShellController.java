package no.ntnu.idatt2003.group38.controller.shell;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import no.ntnu.idatt2003.group38.controller.EndController;
import no.ntnu.idatt2003.group38.controller.dashboard.DashboardController;
import no.ntnu.idatt2003.group38.controller.market.StockMarketController;
import no.ntnu.idatt2003.group38.controller.portfolio.PortfolioController;
import no.ntnu.idatt2003.group38.controller.statistics.WeeklyStatisticsController;
import no.ntnu.idatt2003.group38.controller.transaction.TransactionHistoryController;
import no.ntnu.idatt2003.group38.event.EventNotice;
import no.ntnu.idatt2003.group38.event.Rumor;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.filehandling.GameSaveFileWriter;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.EndView;
import no.ntnu.idatt2003.group38.view.components.ConfirmDialog;
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
  private final GameSaveFileWriter gameSaveFileWriter;

  private final DecimalFormat moneyFormat;
  private final PauseTransition saveStatusReset;

  private Page currentPage;

  private static final Path SAVE_DIRECTORY = Path.of("saves");
  private static final Path AUTOSAVE_PATH = Path.of("saves", "autosave.json");

  /**
   * Creates a new shell controller, wires the side nav, observes the model and
   * mounts the initial page.
   *
   * @param shell    the shell view to control. Must not be {@code null}
   * @param stage    the primary stage. Must not be {@code null}
   * @param player   the player for this game. Must not be {@code null}
   * @param exchange the exchange for this game. Must not be {@code null}
   */
  public ShellController(ShellView shell, Stage stage, Player player, Exchange exchange) {
    this.shell = Objects.requireNonNull(shell, "shell cannot be null");
    this.stage = Objects.requireNonNull(stage, "stage cannot be null");
    this.player = Objects.requireNonNull(player, "player cannot be null");
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");

    this.gameSaveFileWriter = new GameSaveFileWriter();
    this.shell.getTopBar().setOnSaveClicked(this::handleSaveProgress);

    this.shell.getTopBar().setOnEndGameClicked(this::handleEndGame);

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
    symbols.setGroupingSeparator(' ');
    this.moneyFormat = new DecimalFormat("#,##0", symbols);
    this.saveStatusReset = new PauseTransition(Duration.seconds(2));
    this.saveStatusReset.setOnFinished(event -> this.shell.getTopBar().clearSaveStatus());

    this.shell.getSideNav().setOnNavigate(this::navigateTo);
    this.shell.getTopBar().setOnAdvanceClicked(this::handleAdvanceWeek);
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
      case DASHBOARD -> new DashboardController(this.exchange, this.player);
      case MARKET -> new StockMarketController(this.exchange, this.player, this.shell);
      case PORTFOLIO -> new PortfolioController(this.exchange, this.player, this.shell);
      case TRANSACTIONS -> new TransactionHistoryController(this.exchange, this.player);
      case WEEKLY_STATISTICS -> new WeeklyStatisticsController(this.exchange);
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

  /**
   * Opens a confirmation dialog before advancing the market to the next week.
   *
   * <p>When confirmed, the exchange advances, the player's current net worth is
   * recorded for charting purposes, and the active dashboard is refreshed if
   * it is currently mounted.</p>
   */
  private void handleAdvanceWeek() {
    ConfirmDialog dialog = new ConfirmDialog(
        "Advance to Week " + (this.exchange.getWeek() + 1) + "?",
        "All stock prices will update for the new trading week. This cannot be undone.",
        "Advance");
    dialog.setOnCancel(this.shell::hideModal);
    dialog.setOnConfirm(() -> {
      this.shell.hideModal();
      this.exchange.advance();
      this.player.recordNetWorthSnapshot();
      writeAutosave();

      if (this.currentPage instanceof DashboardController dashboardController) {
        dashboardController.refreshChart();
      }

      List<EventNotice> events = this.exchange.getLastWeekEvents();
      List<Rumor> rumors = this.exchange.getActiveRumors();
      if (!events.isEmpty() || !rumors.isEmpty()) {
        showNewsDialog(events, rumors);
      }
    });

    this.shell.showModal(dialog.getRoot());
  }

  /**
   * Opens a confirmation dialog before ending the current game.
   */
  private void handleEndGame() {
    ConfirmDialog dialog = new ConfirmDialog(
        "Sell all holdings and end game?",
        "All owned shares will be sold before the game summary is shown",
        "End Game");

    dialog.setOnCancel(this.shell::hideModal);
    dialog.setOnConfirm(() -> {
      this.shell.hideModal();

      try {
        if (!this.player.getPortfolio().getShares().isEmpty()) {
          this.exchange.sellAll(this.player);
        }
        navigateToEndSummary();
      } catch (IllegalArgumentException | IllegalStateException e) {
        showError("Could not end game: " + e.getMessage());
      }
    });

    this.shell.showModal(dialog.getRoot());
  }

  /**
   * Navigates from the shell to the end-of-game summary screen.
   */
  private void navigateToEndSummary() {
    EndView endView = new EndView();
    new EndController(endView, this.stage, this.player, this.exchange);

    Scene scene = new Scene(endView.getRoot(), 760, 520);
    endView.attachTo(scene);

    dispose();
    this.stage.setScene(scene);
    this.stage.centerOnScreen();
  }

  /**
   * Opens a file chooser and writes the current game state to disk as JSON.
   */
  private void handleSaveProgress() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Save Game");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
    chooser.setInitialFileName(buildDefaultSaveFileName());
    configureSaveDirectory(chooser, true);

    File selected = chooser.showSaveDialog(this.stage);
    if (selected == null) {
      return;
    }

    try {
      this.gameSaveFileWriter.write(selected.toPath(), this.player, this.exchange);
      showSaveStatus("Saved");
    } catch (IOException e) {
      showError("Could not save game: " + e.getMessage());
    }
  }

  /**
   * Builds a default filename for the current save.
   *
   * @return a filename like {@code "sigurd-week-4.json"}
   */
  private String buildDefaultSaveFileName() {
    String playerName = this.player.getName().trim().replaceAll("\\s+", "-");
    return playerName + "-week-" + this.exchange.getWeek() + ".json";
  }

  /**
   * Configures the save chooser to use the application's save directory.
   *
   * @param chooser           the file chooser to configure
   * @param createWhenMissing whether the save directory should be created if missing
   */
  private void configureSaveDirectory(FileChooser chooser, boolean createWhenMissing) {
    try {
      if (createWhenMissing) {
        Files.createDirectories(SAVE_DIRECTORY);
      }

      if (Files.isDirectory(SAVE_DIRECTORY)) {
        chooser.setInitialDirectory(SAVE_DIRECTORY.toFile());
      }
    } catch (IOException e) {
      showError("Could not open save directory: " + e.getMessage());
    }
  }

  /**
   * Writes the current game state to the autosave location.
   */
  private void writeAutosave() {
    try {
      this.gameSaveFileWriter.write(AUTOSAVE_PATH, this.player, this.exchange);
      showSaveStatus("Autosaved");
    } catch (IOException e) {
      showError("Could not write autosave: " + e.getMessage());
    }
  }

  /**
   * Shows a modal news dialog summarising the events that fired this week
   * and any rumors circulating about next week.
   *
   * @param events the events to summarise. Must not be {@code null}.
   * @param rumors the rumors to surface. Must not be {@code null}.
   */
  private void showNewsDialog(List<EventNotice> events, List<Rumor> rumors) {
    StringBuilder body = new StringBuilder();

    if (!events.isEmpty()) {
      body.append("This week:\n");
      for (EventNotice notice : events) {
        body.append("• ")
            .append(notice.stock().getSymbol())
            .append(" — ")
            .append(notice.headline())
            .append("\n");
      }
    }

    if (!rumors.isEmpty()) {
      if (!events.isEmpty()) {
        body.append("\n");
      }
      body.append("On the street:\n");
      for (Rumor rumor : rumors) {
        body.append("• ").append(rumor.getHeadline()).append("\n");
      }
    }

    ConfirmDialog dialog = new ConfirmDialog(
        "Trading news!",
        body.toString(),
        "Continue");
    dialog.setOnConfirm(this.shell::hideModal);
    dialog.setOnCancel(this.shell::hideModal);
    this.shell.showModal(dialog.getRoot());
  }

  /**
   * Shows a short save-status message in the top bar.
   *
   * @param message the message to display
   */
  private void showSaveStatus(String message) {
    this.shell.getTopBar().setSaveStatus(message);
    this.saveStatusReset.stop();
    this.saveStatusReset.playFromStart();
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
    this.saveStatusReset.stop();
    this.exchange.removeObserver(this);
  }

  /**
   * Returns the stage currently used by the shell.
   *
   * @return the primary stage
   */
  public Stage getStage() {
    return stage;
  }
}
