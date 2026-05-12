package no.ntnu.idatt2003.group38.view.components;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

/**
 * A small chart that draws a stock's price history.
 *
 * <p>The component is rendered as a single {@link Polyline} inside a fixed-size
 * {@link Pane}.
 */
public class PriceSparkline {

  private static final double DEFAULT_WIDTH = 250;
  private static final double DEFAULT_HEIGHT = 150;
  private static final double VERTICAL_PADDING = 4;

  private final Pane root;
  private final Polyline line;

  /**
   * Builds a new sparkline at the default size with no data.
   */
  public PriceSparkline() {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  /**
   * Builds a new sparkline at a custom size with no data.
   *
   * @param width the width of the chart area in pixels
   * @param height the height of the chart area in pixels
   */
  public PriceSparkline(double width, double height) {
    this.root = new Pane();
    this.root.setPrefSize(width, height);
    this.root.setMinSize(width, height);
    this.root.setMaxSize(width, height);
    this.root.getStyleClass().add("sparkline");

    this.line = new Polyline();
    this.line.getStyleClass().add("sparkline-line");
    this.root.getChildren().add(this.line);
  }

  /**
   * Returns the root node so it can be mounted in a layout.
   *
   * @return the root node
   */
  public Pane getRoot() {
    return this.root;
  }

  /**
   * Replaces the chart data with the given prices.
   *
   * <p>If the list is empty the chart is cleared. If it has a single price the
   * chart shows a flat line through the middle of the area.
   *
   * @param prices the prices to render, in chronological order. Must not be {@code null}
   */
  public void setPrices(List<BigDecimal> prices) {
    Objects.requireNonNull(prices, "prices cannot be null");
    this.line.getPoints().clear();
    applyTrendClass(prices);

    if (prices.isEmpty()) {
      return;
    }

    double width = this.root.getPrefWidth();
    double height = this.root.getPrefHeight();
    double drawHeight = height - 2 * VERTICAL_PADDING;

    BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElseThrow();
    BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElseThrow();
    double range = max.subtract(min).doubleValue();

    if (prices.size() == 1 || range == 0.0) {
      double y = height / 2.0;
      this.line.getPoints().addAll(0.0, y, width, y);
      return;
    }

    double xStep = width / (prices.size() - 1);
    for (int i = 0; i < prices.size(); i++) {
      double x = i * xStep;
      double normalized = prices.get(i).subtract(min).doubleValue() / range;
      // Higher prices should sit higher visually, so invert the y axis.
      double y = VERTICAL_PADDING + (1.0 - normalized) * drawHeight;
      this.line.getPoints().addAll(x, y);
    }
  }

  /**
   * Sets the trend style class on the line based on the overall direction
   * of the price history.
   *
   * @param prices the prices to inspect
   */
  private void applyTrendClass(List<BigDecimal> prices) {
    this.line.getStyleClass().removeAll(
        "sparkline-up", "sparkline-down", "sparkline-flat");

    if (prices.size() < 2) {
      this.line.getStyleClass().add("sparkline-flat");
      return;
    }

    BigDecimal first = prices.getFirst();
    BigDecimal last = prices.getLast();
    int cmp = last.compareTo(first);
    if (cmp > 0) {
      this.line.getStyleClass().add("sparkline-up");
    } else if (cmp < 0) {
      this.line.getStyleClass().add("sparkline-down");
    } else {
      this.line.getStyleClass().add("sparkline-flat");
    }
  }
}