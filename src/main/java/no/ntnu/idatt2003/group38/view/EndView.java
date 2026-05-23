package no.ntnu.idatt2003.group38.view;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The end view of the Millions application
 *
 * <p>Shows a summary of the finished game and lets the user either
 * start a new game, or exit the application </p>
 *
 * <p>The view does not perform navigation or business logic. The controller
 * is responsible for pushing summary values into the view and wiring button actions</p>
 */
public class EndView {

    private static final String STYLESHEET = "/stylesheets/start-view.css";

    private final VBox root;

    private final Label titleLabel;
    private final Label subtitleLabel;

    private final Label playerLabel;
    private final Label weekLabel;
    private final Label statusLabel;
    private final Label cashLabel;
    private final Label portfolioLabel;
    private final Label netWorthLabel;
    private final Label transactionsLabel;

    private final Button startNewGameButton;
    private final Button exitButton;

    private static final double CONTENT_WIDTH = 320;

    /**
     * Builds the end view and all of its visual components
     */
    public EndView() {
        this.titleLabel = new Label("MILLIONS");
        this.titleLabel.getStyleClass().add("start-view-title");

        this.subtitleLabel = new Label("Game Summary");
        this.subtitleLabel.getStyleClass().add("field-label");

        this.playerLabel =  createSummaryLabel();
        this.weekLabel = createSummaryLabel();
        this.statusLabel = createSummaryLabel();
        this.cashLabel = createSummaryLabel();
        this.portfolioLabel = createSummaryLabel();
        this.netWorthLabel = createSummaryLabel();
        this.transactionsLabel = createSummaryLabel();

        VBox summaryBox = new VBox(
                8,
                this.playerLabel,
                this.weekLabel,
                this.statusLabel,
                this.cashLabel,
                this.portfolioLabel,
                this.netWorthLabel,
                this.transactionsLabel
        );

        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setMaxWidth(CONTENT_WIDTH);

        this.startNewGameButton = new  Button("Start New Game");
        this.startNewGameButton.getStyleClass().add("start-button");

        this.exitButton = new Button("Exit Application");
        this.exitButton.getStyleClass().add("start-button");

        VBox actions = new VBox(12, this.startNewGameButton, this.exitButton);
        actions.setAlignment(Pos.CENTER);

        this.root = new VBox(24, this.tittleLabel, this.subtitleLabel, summaryBox, actions);
        this.root.setAlignment(Pos.CENTER);
        this.root.getStyleClass().add("start-view");

        setSummary("Player", 1, "Novice", "0", "0", "0", 0);
    }

    /**
     * Returns the root node so the application can attach it to a scene
     *
     * @return the root layout container of the end view
     */
    public VBox getRoot() {
        return root;
    }

    /**
     * Attaches the end-view stylesheet to the given scene
     *
     * @param scene the scene to attach the stylesheet to, must not be {@code null}
     */
    public void attachTo(Scene scene) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        String css = Objects.requireNonNull(getClass().getResource(STYLESHEET), "Could not find stylesheet at " + STYLESHEET).toExternalForm();

        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    /**
     * Updates the summary labels shown in the view
     *
     * @param playerName the player name
     * @param week the last played week
     * @param status the player's final status
     * @param cash the formatted final cash balance
     * @param portfolioValue the formatted final portfolio value
     * @param netWorth the formatted final net worth
     * @param transaction the total number of transactions
     */
    public void setSummary(
            String playerName,
            int week,
            String status,
            String cash,
            String portfolioValue,
            String netWorth,
            int transaction
    ) {
        this.playerLabel.setText("Player: " + playerName);
        this.weekLabel.setText("Week: " + week);
        this.statusLabel.setText("Status: " + status);
        this.cashLabel.setText("Cash: " + cash);
        this.portfolioLabel.setText("Portfolio Value: " + portfolioValue);
        this.netWorthLabel.setText("Net Worth: " + netWorth);
        this.transactionsLabel.setText("Transactions: " + transaction);
    }

    /**
     * Registers the action ro run when the user clicks "Start New Game"
     *
     * @param action the action to execute, must not be {@code null}
     */
    public void setOnStartNewGame(Runnable action) {
        Objects.requireNonNull(action, "Action cannot be null");
        this.startNewGameButton.setOnAction(e -> action.run());
    }

    /**
     * Registers the action to run when the user clicks "Exit Application"
     *
     * @param action the action to execute, must not be {@code null}
     */
    public void setOnExit(Runnable action) {
        Objects.requireNonNull(action, "Action cannot be null");
        this.exitButton.setOnAction(e -> action.run());
    }

    private Label createSummaryLabel() {
        Label label = new Label();
        label.getStyleClass().add("field-label");
        label.setMaxWidth(CONTENT_WIDTH);
        label.setWrapText(true);
        return label;
    }
}
