package no.ntnu.idatt2003.group38.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import no.ntnu.idatt2003.group38.view.LauncherView;
import no.ntnu.idatt2003.group38.view.StartView;
import no.ntnu.idatt2003.group38.controller.shell.ShellController;
import no.ntnu.idatt2003.group38.filehandling.GameSaveFileReader;
import no.ntnu.idatt2003.group38.filehandling.LoadedGame;
import no.ntnu.idatt2003.group38.view.shell.ShellView;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Controller for {@link LauncherView}
 *
 * <p>Handles launcher actions and scene navigation to the new-game form</p>
 */
public class LauncherController {

    private static final Path SAVE_DIRECTORY = Path.of("saves");

    private final LauncherView view;
    private final Stage stage;
    private final GameSaveFileReader gameSaveFileReader;

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
        this.gameSaveFileReader = new GameSaveFileReader();
    }

    /**
     * Opens the new-game form
     */
    private void handleNewGame() {
        navigateToStartGame();
    }

    /**
     * POpens a save-file picker and loads the selected saved game
     */
    private void handleLoadGame() {
        this.view.showError(null);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Saved Game");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        configureSaveDirectory(chooser);

        File selected = chooser.showOpenDialog(this.stage);
        if (selected == null) {
            return;
        }

        try {
            LoadedGame loadedGame = this.gameSaveFileReader.read(selected.toPath());
            navigateToShell(loadedGame.player(), loadedGame.exchange());
        } catch (IOException e) {
            this.view.showError("Could not load saved game: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            this.view.showError("Invalid save file: " + e.getMessage());
        }
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

    /**
     * Navigates to the main game shell with the given restored game state.
     *
     * @param player the player to continue with
     * @param exchange the exchange to continue with
     */
    private void navigateToShell(Player player, Exchange exchange) {
        ShellView shellView = new ShellView();
        new ShellController(shellView, this.stage, player, exchange);

        Scene scene = new Scene(shellView.getRoot(), 1024, 720);
        shellView.attachTo(scene);

        this.stage.setScene(scene);
        this.stage.centerOnScreen();
    }

    /**
     * Configures the file chooser to open in the application's save directory.
     *
     * @param chooser the chooser to configure
     */
    private void configureSaveDirectory(FileChooser chooser) {
        if (Files.isDirectory(SAVE_DIRECTORY)) {
            chooser.setInitialDirectory(SAVE_DIRECTORY.toFile());
        }
    }

}
