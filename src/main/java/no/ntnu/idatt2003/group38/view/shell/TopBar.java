package no.ntnu.idatt2003.group38.view.shell;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * The top bar of the application shell.
 *
 * <p>Displays the application title on the left and global game state on the
 * right: current week, available cash, net worth, player status and
 * top-level game actions.
 */
public class TopBar {

  private final HBox root;
  private final Label weekLabel;
  private final Label cashLabel;
  private final Label netWorthLabel;
  private final Label statusLabel;
  private final Label saveStatusLabel;
  private final Button nextWeekButton;
  private final Button saveButton;
  private final Button endGameButton;

  /**
   * Builds the top bar with placeholder values for week, cash, net worth and
   * status. Real values must be pushed in by a controller after construction.
   */
  public TopBar() {
    Label title = new Label("Millions");
    title.getStyleClass().add("top-bar-title");

    this.weekLabel = new Label();
    this.weekLabel.getStyleClass().add("top-bar-info");

    this.cashLabel = new Label();
    this.cashLabel.getStyleClass().add("top-bar-info");

    this.netWorthLabel = new Label();
    this.netWorthLabel.getStyleClass().addAll("top-bar-info", "top-bar-net-worth");

    this.statusLabel = new Label();
    this.statusLabel.getStyleClass().add("top-bar-info");

    this.saveStatusLabel = new Label();
    this.saveStatusLabel.getStyleClass().add("top-bar-save-status");
    this.saveStatusLabel.setVisible(false);
    this.saveStatusLabel.setManaged(false);

    this.nextWeekButton = new Button("Next week");
    this.nextWeekButton.getStyleClass().add("top-bar-next-week");

    this.saveButton = new Button("Save");
    this.saveButton.getStyleClass().add("top-bar-next-week");

    this.endGameButton = new Button("End game");
    this.endGameButton.getStyleClass().add("top-bar-next-week");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox infoGroup = new HBox(32,
        this.weekLabel, this.cashLabel, this.netWorthLabel, this.statusLabel,
        this.saveStatusLabel, this.saveButton, this.endGameButton, this.nextWeekButton);
    infoGroup.setAlignment(Pos.CENTER_RIGHT);
    infoGroup.getStyleClass().add("top-bar-info-group");

    this.root = new HBox(title, spacer, infoGroup);
    this.root.setAlignment(Pos.CENTER_LEFT);
    this.root.getStyleClass().add("top-bar");

    // Sensible defaults so the bar is never empty on first render.
    setWeek(1);
    setCash("0");
    setNetWorth("0");
    setStatus("Investor");
  }

  /**
   * Returns the root node so the shell can mount the top bar.
   *
   * @return the root layout container of the top bar
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Updates the week indicator.
   *
   * @param week the current trading week
   */
  public void setWeek(int week) {
    this.weekLabel.setText("Week " + week);
  }

  /**
   * Updates the cash indicator.
   *
   * @param formattedCash a pre-formatted cash amount (the controller decides
   *                      how to format the {@code BigDecimal})
   */
  public void setCash(String formattedCash) {
    this.cashLabel.setText("Cash: " + formattedCash);
  }

  /**
   * Updates the net worth indicator.
   *
   * @param formattedNetWorth a pre-formatted net worth amount
   */
  public void setNetWorth(String formattedNetWorth) {
    this.netWorthLabel.setText("Net Worth: " + formattedNetWorth);
  }

  /**
   * Updates the status indicator (for example "Investor", "Trader", etc.).
   *
   * @param status the player's current status
   */
  public void setStatus(String status) {
    this.statusLabel.setText("Status: " + status);
  }

  /**
   * Registers the action to invoke when the user clicks the Next week button.
   *
   * @param onAdvance the action. Must not be {@code null}
   */
  public void setOnAdvanceClicked(Runnable onAdvance) {
    this.nextWeekButton.setOnAction(e -> onAdvance.run());
  }

  /**
   * Registers the action to invoke when the user clicks the Save button.
   *
   * @param onSave the action. Must not be {@code null}
   */
  public void setOnSaveClicked(Runnable onSave) {
    this.saveButton.setOnAction(e -> onSave.run());
  }

  /**
   * Registers the action to invoke when the user clicks the End Game button
   *
   * @param onEndGame the action, must not be {@code null}
   */
  public void setOnEndGameClicked(Runnable onEndGame) {
    this.endGameButton.setOnAction(e -> onEndGame.run());
  }

  /**
   * Shows a short save-status message in the top bar.
   *
   * @param message the save-status message to display
   */
  public void setSaveStatus(String message) {
    this.saveStatusLabel.setText(message);
    this.saveStatusLabel.setVisible(true);
    this.saveStatusLabel.setManaged(true);
  }

  /**
   * Clears any save-status message currently shown in the top bar.
   */
  public void clearSaveStatus() {
    this.saveStatusLabel.setText("");
    this.saveStatusLabel.setVisible(false);
    this.saveStatusLabel.setManaged(false);
  }
}
