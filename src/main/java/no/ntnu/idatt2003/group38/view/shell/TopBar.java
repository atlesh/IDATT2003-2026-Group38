package no.ntnu.idatt2003.group38.view.shell;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * The top bar of the application shell.
 *
 * <p>Displays the application title on the left and global game state on the
 * right: current week, available cash, net worth and player status.
 */
public class TopBar {

  private final HBox root;
  private final Label weekLabel;
  private final Label cashLabel;
  private final Label netWorthLabel;
  private final Label statusLabel;

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

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox infoGroup = new HBox(32,
        this.weekLabel, this.cashLabel, this.netWorthLabel, this.statusLabel);
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
}
