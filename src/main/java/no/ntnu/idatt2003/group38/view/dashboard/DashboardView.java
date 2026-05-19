package no.ntnu.idatt2003.group38.view.dashboard;

import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

    private final Button nextWeekButton;
    private Runnable onAdvanceWeek = () -> { };

    public DashboardView() {
        this.welcomeLabel = new Label("Welcome");
        this.welcomeLabel.getStyleClass().add("market-title");

        this.statusLabel = new Label("Week 1 | Status: Novice");
        this.statusLabel.getStyleClass().add("stock-card-line");

        Label dashboardTitle =  new Label("Dashboard");
        dashboardTitle.getStyleClass().add("stock-card-title");

        VBox heroPanel = new VBox(8, dashboardTitle, this.welcomeLabel, this.statusLabel);
        heroPanel.getStyleClass().add("market-panel");
        HBox.setHgrow(heroPanel, Priority.ALWAYS);

        Label actionTitle = new Label("Next week");
        actionTitle.getStyleClass().add("stock-card-title");

        Label actionText = new Label("Advance the market to the next trading week.");
        actionText.getStyleClass().add("stock-card-line");
        actionText.setWrapText(true);

        this.nextWeekButton = new Button("Advance Week");
        this.nextWeekButton.getStyleClass().add("buy-button");
        this.nextWeekButton.setOnAction(event -> this.onAdvanceWeek.run());

        VBox actionPanel = new VBox(10, actionTitle, actionText, this.nextWeekButton);
        actionPanel.getStyleClass().addAll("market-panel", "stock-card");
        actionPanel.setAlignment(Pos.TOP_LEFT);

        HBox topRow = new HBox(20, heroPanel, actionPanel);

        this.cashValueLabel = createMetricValueLabel();
        this.portfolioValueLabel = createMetricValueLabel();
        this.netWorthValueLabel = createMetricValueLabel();
        this.positionsValueLabel = createMetricValueLabel();

        HBox metricsRow = new HBox(
                20,
                buildMetricCard("Cash", this.cashValueLabel),
                buildMetricCard("Portfolio Value", this.portfolioValueLabel),
                buildMetricCard("Net Worth", this.netWorthValueLabel),
                buildMetricCard("Open Positions", this.positionsValueLabel)
        );

        this.gainersBox = new VBox(8);
        this.losersBox = new VBox(8);

        Label gainersTitle = new Label("Top Gainers");
        gainersTitle.getStyleClass().add("stock-card-title");

        Label losersTitle = new Label("Top Losers");
        losersTitle.getStyleClass().add("stock-card-title");

        VBox gainersColumn =  new VBox(8, gainersTitle, this.gainersBox);
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
                this.cashRatioLabel
        );

        snapshotPanel.getStyleClass().addAll("market-panel", "stock-card");
        snapshotPanel.setAlignment(Pos.TOP_LEFT);

        HBox middleRow = new HBox(20, marketMoversPanel, snapshotPanel);

        Label recentTitle = new Label("Recent Activity");
        recentTitle.getStyleClass().add("stock-card-title");

        this.recentActivityBox = new VBox(8);

        VBox recentActivityPanel = new VBox(12, recentTitle, this.recentActivityBox);
        recentActivityPanel.getStyleClass().add("market-panel");

        this.content = new VBox(20, topRow, metricsRow, middleRow, recentActivityPanel);
        this.content.setPadding(new Insets(20));
        this.content.getStyleClass().add("market-view");

        this.root = new ScrollPane(this.content);
        this.root.setFitToWidth(true);
        this.root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.root.getStyleClass().add("market-scroll");

        setHeader("Player", 1, "Novice");
        setOverview("0", "0", "0", "0");
        setPortfolioSnapshot("No positions", "No positions", "No positions", "%");
        setMarketMovers(List.of("No market movement yet"), List.of("No market movement yet"));
        setRecentActivity(List.of("No transactions yet"));
    }

    public Region getRoot() {
        return this.root;
    }

    public void attachTo(Scene scene) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        String css = Objects.requireNonNull(getClass().getResource(STYLESHEET), "Could not find stylesheet at " + STYLESHEET).toExternalForm();

        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    public void setOnAdvanceWeek(Runnable action) {
        this.onAdvanceWeek = Objects.requireNonNull(action, "Action cannot be null");
    }

    public void setHeader(String playerName, int week, String status) {
        this.welcomeLabel.setText("Welcome, " + playerName);
        this.statusLabel.setText("Week "+ week + " | Status: " + status);
    }

    public void setOverview(String cash, String portfolioValue, String netWorth, String positions) {
        this.cashValueLabel.setText(cash);
        this.portfolioValueLabel.setText(portfolioValue);
        this.netWorthValueLabel.setText(netWorth);
        this.positionsValueLabel.setText(positions);
    }

    public void setPortfolioSnapshot(String largest, String best, String worst, String cashRatio) {
        this.largestPositionLabel.setText("Largest position: " + largest);
        this.bestHoldingLabel.setText("Best holding: " + best);
        this.worstHoldingLabel.setText("Worst holding: " + worst);
        this.cashRatioLabel.setText("Cash ratio: " + cashRatio);
    }

    public void setMarketMovers(List<String> gainers, List<String> losers) {
        replaceRows(this.gainersBox, gainers, "change-positive");
        replaceRows(this.losersBox, losers, "change-negative");
    }

    public void setRecentActivity(List<String> rows) {
        replaceRows(this.recentActivityBox, rows, null);
    }

    public VBox buildMetricCard(String titleText, Label valueLabel) {
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("stock-card-title");

        VBox card = new VBox(8 ,titleLabel, valueLabel);
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

    private void replaceRows(VBox container, List<String> rows, String extraStyleClass) {
        Objects.requireNonNull(container, "Container cannot be null");
        Objects.requireNonNull(rows, "Rows cannot be null");

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
