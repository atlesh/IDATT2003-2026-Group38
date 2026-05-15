package no.ntnu.idatt2003.group38.view.statistics;

import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.view.components.StatPanel;

/**
 * The Weekly Statistics page view.
 *
 * <p>Shows two {@link StatPanel}s. The week's biggest gainers on
 * the left and the week's biggest losers on the right.
 */
public class WeeklyStatisticsView {

  private static final String STYLESHEET = "/stylesheets/weekly.css";

  private final VBox root;
  private final StatPanel gainersPanel;
  private final StatPanel losersPanel;

  /**
   * Builds the weekly statistics view with two empty panels.
   */
  public WeeklyStatisticsView() {
    Label title = new Label("Weekly Statistics");
    title.getStyleClass().add("weekly-title");

    this.gainersPanel = new StatPanel("Weekly Gainers");
    this.losersPanel = new StatPanel("Weekly Losers");

    HBox panels = new HBox(20, this.gainersPanel.getRoot(), this.losersPanel.getRoot());
    HBox.setHgrow(this.gainersPanel.getRoot(), Priority.ALWAYS);
    HBox.setHgrow(this.losersPanel.getRoot(), Priority.ALWAYS);
    VBox.setVgrow(panels, Priority.ALWAYS);

    this.root = new VBox(20, title, panels);
    this.root.setPadding(new Insets(20));
    this.root.getStyleClass().add("weekly-view");
  }

  /**
   * Returns the root node so the controller can mount the view.
   *
   * @return the root layout container of the view
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Updates the gainers panel.
   *
   * @param stocks the gainers. Must not be {@code null}
   */
  public void setGainers(List<Stock> stocks) {
    this.gainersPanel.setStocks(stocks);
  }

  /**
   * Updates the losers panel.
   *
   * @param stocks the losers. Must not be {@code null}
   */
  public void setLosers(List<Stock> stocks) {
    this.losersPanel.setStocks(stocks);
  }

  /**
   * Attaches this stylesheet to the given scene.
   *
   * @param scene the scene to attach the stylesheet to. Must not be {@code null}
   */
  public void attachTo(Scene scene) {
    Objects.requireNonNull(scene, "scene cannot be null");
    String css = Objects.requireNonNull(
        getClass().getResource(STYLESHEET),
        "Could not find stylesheet at " + STYLESHEET).toExternalForm();
    if (!scene.getStylesheets().contains(css)) {
      scene.getStylesheets().add(css);
    }
  }
}