package no.ntnu.idatt2003.group38.controller;

import java.util.Objects;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.ntnu.idatt2003.group38.view.LauncherView;
import no.ntnu.idatt2003.group38.view.StartView;

/**
 * Controller for {@link LauncherView}
 *
 * <p>Handles launcher actions and scene navigation to the new-game form</p>
 */
public class LauncherController {

    private final LauncherView view;
    private final Stage stage;

    /**
     * Creates a new launcher controller and wires the view actions
     *
     * @param view the launcher view to control, must not be {@code null}
     * @param stage the primary stage used for scene transitions, must not be {@code null}
     */
    public LauncherController(LauncherView view, Stage stage) {
        this.view = Objects.requireNonNull(view, "View cannot be null");
        this.stage = Objects.requireNonNull(stage, "Stage cannot be null");

        this.view.setOnNewGame(this::handleNewGame);
        this.view.setOnLoadGame(this::handleLoadGame);
        this.view.setOnExit(this::handleExit);
    }

    /**
     * Opens the new-game form
     */
    private void handleNewGame() {
        navigateToStartGame();
    }

    /**
     * Placeholder until save/load game is implemented
     */
    private void handleLoadGame() {
        this.view.showError("Load saved game is not implemented yet");
    }

    /**
     * Close the application
     */
    private void handleExit() {
        this.stage.close();
    }

    /**
     * Navigates to the existing start-game form
     */
    private void navigateToStartGame() {
        StartView startView = new StartView();
        new StartController(startView, this.stage);

        Scene scene = new Scene(startView.getRoot(), 760, 480);
        startView.attachTo(scene);

        this.stage.setScene(scene);
        this.stage.centerOnScreen();
    }
}
