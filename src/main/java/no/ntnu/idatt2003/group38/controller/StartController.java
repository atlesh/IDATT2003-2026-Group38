package no.ntnu.idatt2003.group38.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import no.ntnu.idatt2003.group38.controller.shell.ShellController;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.filehandling.CsvStockReader;
import no.ntnu.idatt2003.group38.filehandling.InvalidStockDataException;
import no.ntnu.idatt2003.group38.filehandling.StockFileReader;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.view.StartView;
import no.ntnu.idatt2003.group38.view.shell.ShellView;

/**
 * Controller for {@link StartView}.
 *
 * <p>Wires user interactions on the start screen to model creation and scene
 * navigation.
 *
 * <p>All validation errors are surfaced to the user through
 * {@link StartView#showError(String)} rather than thrown.
 */
public class StartController {

  private static final String EXCHANGE_NAME = "Millions Exchange";

  private final StartView view;
  private final Stage stage;
  private final StockFileReader stockFileReader;

  /**
   * Creates a new controller and wires the view's actions.
   *
   * @param view the start view to control. Must not be {@code null}
   * @param stage the primary stage used for scene transitions. Must not be {@code null}
   */
  public StartController(StartView view, Stage stage) {
    this.view = Objects.requireNonNull(view, "view cannot be null");
    this.stage = Objects.requireNonNull(stage, "stage cannot be null");
    this.stockFileReader = new CsvStockReader();

    this.view.setOnPickFile(this::handlePickFile);
    this.view.setOnStartGame(this::handleStartGame);
  }

  // File picker

  /**
   * Opens a {@link FileChooser} so the user can pick a CSV stock file from
   * anywhere on the system.
   */
  private void handlePickFile() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Pick Stock File");
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"),
        new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));
    chooser.setInitialDirectory(new File(System.getProperty("user.home")));

    File selected = chooser.showOpenDialog(this.stage);
    if (selected != null) {
      this.view.setSelectedFile(selected);
      this.view.showError(null);
    }
  }

  // Start Game

  /**
   * Validates all input, builds the model and switches to the dashboard scene.
   * If anything fails, an error message is shown in the view.
   */
  private void handleStartGame() {
    this.view.showError(null);

    String username = this.view.getUsername();
    if (username.isEmpty()) {
      this.view.showError("Please enter a username.");
      return;
    }

    BigDecimal startingCapital;
    try {
      startingCapital = parseCapital(this.view.getStartingCapital());
    } catch (IllegalArgumentException e) {
      this.view.showError(e.getMessage());
      return;
    }

    File stockFile = this.view.getStockFile();
    if (stockFile == null) {
      this.view.showError("Please pick a stock file.");
      return;
    }
    if (!stockFile.isFile() || !stockFile.canRead()) {
      this.view.showError("The selected file cannot be read.");
      return;
    }

    List<Stock> stocks;
    try {
      stocks = this.stockFileReader.readStocks(stockFile.toPath());
    } catch (InvalidStockDataException e) {
      this.view.showError("Invalid stock file: " + e.getMessage());
      return;
    } catch (IOException e) {
      this.view.showError("Could not read stock file: " + e.getMessage());
      return;
    }
    if (stocks.isEmpty()) {
      this.view.showError("The selected file did not contain any valid stocks.");
      return;
    }

    Player player = new Player(username, startingCapital);
    Exchange exchange = new Exchange(EXCHANGE_NAME, stocks);

    navigateToDashboard(player, exchange);
  }

  /**
   * Parses the raw starting-capital text into a positive {@link BigDecimal}.
   *
   * @param raw the raw text from the view
   * @return the parsed amount
   * @throws IllegalArgumentException if the text is empty, not a number, or not positive
   */
  private BigDecimal parseCapital(String raw) {
    if (raw.isEmpty()) {
      throw new IllegalArgumentException("Please enter a starting capital.");
    }
    BigDecimal amount;
    try {
      amount = new BigDecimal(raw);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Starting capital must be a number (use '.' as decimal separator).");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Starting capital must be greater than 0.");
    }
    return amount;
  }

  // Navigation

  /**
   * Navigates from the start scene to the dashboard.
   *
   * @param player the player that was just created
   * @param exchange the exchange that was just created
   */
  private void navigateToDashboard(Player player, Exchange exchange) {
    ShellView shell = new ShellView();

    Scene scene = new Scene(shell.getRoot(), 1024, 720);
    shell.attachTo(scene);
    this.stage.setScene(scene);

    new ShellController(shell, this.stage, player, exchange);

    this.stage.centerOnScreen();
  }
}
