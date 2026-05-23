package no.ntnu.idatt2003.group38.view.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The dashboard page view.
 *
 * <p>Presents a high-level overview of the current game state, including
 * player metrics, portfolio snapshot, market movers, recent activity and a
 * chart of net-worth history.
 */
public class DashboardView {

  private static final String STYLESHEET = "/stylesheets/market.css";

  private final ScrollPane root;
  private final VBox content;

  private final Label welcomeLabel;
  private final Label statusLabel;

  private final Label cashValueLabel;
  private final Label portfolioValueLabel;
  private final Label netWorthValueLabel;
  private final Label positionsValueLabel;

  private final Label largestPositionLabel;
  private final Label bestHoldingLabel;
  private final Label worstHoldingLabel;
  private final Label cashRatioLabel;

  private final VBox gainersBox;
  private final VBox losersBox;
  private final VBox recentActivityBox;

  private final NumberAxis xAxis;
  private final NumberAxis yAxis;
  private final LineChart<Number, Number> performanceChart;
  private final XYChart.Series<Number, Number> performanceSeries;

  /**
   * Builds the dashboard view and all of its visual sections.
   */
  public DashboardView() {
    this.welcomeLabel = new Label("Welcome");
    this.welcomeLabel.getStyleClass().add("market-title");

    this.statusLabel = new Label("Week 1 | Status: Novice");
    this.statusLabel.getStyleClass().add("stock-card-line");

    Label dashboardTitle = new Label("Dashboard");
    dashboardTitle.getStyleClass().add("stock-card-title");

    VBox heroPanel = new VBox(8, dashboardTitle, this.welcomeLabel, this.statusLabel);
    heroPanel.getStyleClass().add("market-panel");
    HBox.setHgrow(heroPanel, Priority.ALWAYS);

    HBox topRow = new HBox(20, heroPanel);

    this.cashValueLabel = createMetricValueLabel();
    this.portfolioValueLabel = createMetricValueLabel();
    this.netWorthValueLabel = createMetricValueLabel();
    this.positionsValueLabel = createMetricValueLabel();

    HBox metricsRow = new HBox(
        20,
        buildMetricCard("Cash", this.cashValueLabel),
        buildMetricCard("Portfolio Value", this.portfolioValueLabel),
        buildMetricCard("Net Worth", this.netWorthValueLabel),
        buildMetricCard("Open Positions", this.positionsValueLabel));

    Label performanceTitle = new Label("Portfolio Performance");
    performanceTitle.getStyleClass().add("stock-card-title");

    this.xAxis = new NumberAxis();
    this.xAxis.setLabel("Week");
    this.xAxis.setForceZeroInRange(false);
    this.xAxis.setAutoRanging(false);
    this.xAxis.setLowerBound(1);
    this.xAxis.setUpperBound(2);
    this.xAxis.setTickUnit(1);
    this.xAxis.setMinorTickVisible(false);

    this.yAxis = new NumberAxis();
    this.yAxis.setLabel("Net Worth");
    this.yAxis.setForceZeroInRange(false);
    this.yAxis.setAutoRanging(false);
    this.yAxis.setLowerBound(0);
    this.yAxis.setUpperBound(1);
    this.yAxis.setTickUnit(1);
    this.yAxis.setMinorTickVisible(false);

    this.performanceSeries = new XYChart.Series<>();
    this.performanceChart = new LineChart<>(this.xAxis, this.yAxis);
    this.performanceChart.setLegendVisible(false);
    this.performanceChart.setAnimated(false);
    this.performanceChart.setCreateSymbols(false);
    this.performanceChart.setPrefHeight(240);
    this.performanceChart.getData().add(this.performanceSeries);

    VBox performancePanel = new VBox(12, performanceTitle, this.performanceChart);
    performancePanel.getStyleClass().add("market-panel");

    this.gainersBox = new VBox(8);
    this.losersBox = new VBox(8);

    Label gainersTitle = new Label("Top Gainers");
    gainersTitle.getStyleClass().add("stock-card-title");

    Label losersTitle = new Label("Top Losers");
    losersTitle.getStyleClass().add("stock-card-title");

    VBox gainersColumn = new VBox(8, gainersTitle, this.gainersBox);
    VBox losersColumn = new VBox(8, losersTitle, this.losersBox);
    HBox.setHgrow(gainersColumn, Priority.ALWAYS);
    HBox.setHgrow(losersColumn, Priority.ALWAYS);

    HBox moversContent = new HBox(20, gainersColumn, losersColumn);

    Label moversPanelTitle = new Label("Market Movers");
    moversPanelTitle.getStyleClass().add("stock-card-title");

    VBox marketMoversPanel = new VBox(12, moversPanelTitle, moversContent);
    marketMoversPanel.getStyleClass().add("market-panel");
    HBox.setHgrow(marketMoversPanel, Priority.ALWAYS);

    Label snapshotTitle = new Label("Portfolio Snapshot");
    snapshotTitle.getStyleClass().add("stock-card-title");

    this.largestPositionLabel = createDetailLabel();
    this.bestHoldingLabel = createDetailLabel();
    this.worstHoldingLabel = createDetailLabel();
    this.cashRatioLabel = createDetailLabel();

    VBox snapshotPanel = new VBox(
        10,
        snapshotTitle,
        this.largestPositionLabel,
        this.bestHoldingLabel,
        this.worstHoldingLabel,
        this.cashRatioLabel);
    snapshotPanel.getStyleClass().addAll("market-panel", "stock-card");
    snapshotPanel.setAlignment(Pos.TOP_LEFT);

    HBox middleRow = new HBox(20, marketMoversPanel, snapshotPanel);

    Label recentTitle = new Label("Recent Activity");
    recentTitle.getStyleClass().add("stock-card-title");

    this.recentActivityBox = new VBox(8);
    VBox recentActivityPanel = new VBox(12, recentTitle, this.recentActivityBox);
    recentActivityPanel.getStyleClass().add("market-panel");

    this.content = new VBox(20, topRow, metricsRow, performancePanel, middleRow, recentActivityPanel);
    this.content.setPadding(new Insets(20));
    this.content.getStyleClass().add("market-view");

    this.root = new ScrollPane(this.content);
    this.root.setFitToWidth(true);
    this.root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    this.root.getStyleClass().add("market-scroll");

    setHeader("Player", 1, "Novice");
    setOverview("0", "0", "0", "0");
    setPortfolioSnapshot("No positions", "No positions", "No positions", "0.0%");
    setPerformanceHistory(List.of(BigDecimal.ZERO));
    setMarketMovers(List.of("No market movement yet"), List.of("No market movement yet"));
    setRecentActivity(List.of("No transactions yet"));
  }

  /**
   * Returns the root node so the dashboard can be mounted in the shell.
   *
   * @return the root layout container of the dashboard
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Attaches the dashboard stylesheet to the given scene if it is not already present.
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

  /**
   * Updates the welcome area with the current player, week and status.
   *
   * @param playerName the player's display name
   * @param week the current trading week
   * @param status the player's current status
   */
  public void setHeader(String playerName, int week, String status) {
    this.welcomeLabel.setText("Welcome, " + playerName);
    this.statusLabel.setText("Week " + week + " | Status: " + status);
  }

  /**
   * Updates the top-level KPI cards.
   *
   * @param cash the formatted cash value
   * @param portfolioValue the formatted portfolio value
   * @param netWorth the formatted net-worth value
   * @param positions the formatted number of open positions
   */
  public void setOverview(String cash, String portfolioValue, String netWorth, String positions) {
    this.cashValueLabel.setText(cash);
    this.portfolioValueLabel.setText(portfolioValue);
    this.netWorthValueLabel.setText(netWorth);
    this.positionsValueLabel.setText(positions);
  }

  /**
   * Updates the portfolio snapshot panel.
   *
   * @param largest the largest current position
   * @param best the best-performing holding
   * @param worst the weakest-performing holding
   * @param cashRatio the cash ratio as a formatted percentage
   */
  public void setPortfolioSnapshot(String largest, String best, String worst, String cashRatio) {
    this.largestPositionLabel.setText("Largest position: " + largest);
    this.bestHoldingLabel.setText("Best holding: " + best);
    this.worstHoldingLabel.setText("Worst holding: " + worst);
    this.cashRatioLabel.setText("Cash ratio: " + cashRatio);
  }

  /**
   * Replaces the performance chart data with one net-worth value per week.
   *
   * @param history the chronological net-worth history to plot. Must not be {@code null}
   */
  public void setPerformanceHistory(List<BigDecimal> history) {
    Objects.requireNonNull(history, "history cannot be null");

    this.performanceSeries.getData().clear();
    for (int i = 0; i < history.size(); i++) {
      this.performanceSeries.getData().add(
          new XYChart.Data<>(i + 1, history.get(i).doubleValue()));
    }

    updatePerformanceAxes(history);
  }

  /**
   * Updates the market movers section with formatted gainers and losers.
   *
   * @param gainers the rows to show in the top gainers column. Must not be {@code null}
   * @param losers the rows to show in the top losers column. Must not be {@code null}
   */
  public void setMarketMovers(List<String> gainers, List<String> losers) {
    replaceRows(this.gainersBox, gainers, "change-positive");
    replaceRows(this.losersBox, losers, "change-negative");
  }

  /**
   * Updates the recent activity section.
   *
   * @param rows the transaction rows to display. Must not be {@code null}
   */
  public void setRecentActivity(List<String> rows) {
    replaceRows(this.recentActivityBox, rows, null);
  }

  private VBox buildMetricCard(String titleText, Label valueLabel) {
    Label titleLabel = new Label(titleText);
    titleLabel.getStyleClass().add("stock-card-title");

    VBox card = new VBox(8, titleLabel, valueLabel);
    card.getStyleClass().add("market-panel");
    card.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(card, Priority.ALWAYS);
    return card;
  }

  private Label createMetricValueLabel() {
    Label label = new Label("0");
    label.getStyleClass().add("stock-card-line");
    return label;
  }

  private Label createDetailLabel() {
    Label label = new Label();
    label.getStyleClass().add("stock-card-line");
    label.setWrapText(true);
    return label;
  }

  private void updatePerformanceAxes(List<BigDecimal> history) {
    if (history.isEmpty()) {
      this.xAxis.setLowerBound(1);
      this.xAxis.setUpperBound(2);
      this.xAxis.setTickUnit(1);

      this.yAxis.setLowerBound(0);
      this.yAxis.setUpperBound(1);
      this.yAxis.setTickUnit(1);
      return;
    }

    int weeks = Math.max(2, history.size());
    this.xAxis.setLowerBound(1);
    this.xAxis.setUpperBound(weeks);
    this.xAxis.setTickUnit(1);

    double min = history.stream()
        .mapToDouble(BigDecimal::doubleValue)
        .min()
        .orElse(0);
    double max = history.stream()
        .mapToDouble(BigDecimal::doubleValue)
        .max()
        .orElse(1);

    double span = max - min;
    double padding = span == 0 ? Math.max(1.0, Math.abs(max) * 0.1) : span * 0.1;
    double lower = min - padding;
    double upper = max + padding;
    double tickUnit = Math.max(1.0, (upper - lower) / 4.0);

    this.yAxis.setLowerBound(lower);
    this.yAxis.setUpperBound(upper);
    this.yAxis.setTickUnit(tickUnit);
  }

  private void replaceRows(VBox container, List<String> rows, String extraStyleClass) {
    Objects.requireNonNull(container, "container cannot be null");
    Objects.requireNonNull(rows, "rows cannot be null");

    container.getChildren().clear();

    for (String row : rows) {
      Label label = new Label(row);
      label.getStyleClass().add("stock-card-line");
      label.setWrapText(true);

      if (extraStyleClass != null) {
        label.getStyleClass().add(extraStyleClass);
      }

      container.getChildren().add(label);
    }
  }
}
