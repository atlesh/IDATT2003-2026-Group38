package no.ntnu.idatt2003.group38.view;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The launcher view of the Millions application.
 *
 * <p>Lets the user choose whether to start a new game, load a saved game,
 * or exit the application.</p>
 *
 * <p>The view does not perform any navigation or game logic. The controller is
 * responsible for wiring button actions and showing errors.</p>
 */
public class LauncherView {

  private static final String STYLESHEET = "/stylesheets/start-view.css";

  private final VBox root;
  private final Button newGameButton;
  private final Button loadGameButton;
  private final Button exitButton;
  private final Label errorLabel;

  /**
   * Builds the launcher view and all fits components.
   */
  public LauncherView() {
    Label title = new Label("MILLIONS");
    title.getStyleClass().add("start-view-title");

    Label subtitle = new Label("Choose how to continue");
    subtitle.getStyleClass().add("field-label");

    this.newGameButton = new Button("New Game");
    this.newGameButton.getStyleClass().add("start-button");
    this.newGameButton.setMaxWidth(220);

    this.loadGameButton = new Button("Load Saved Game");
    this.loadGameButton.getStyleClass().add("start-button");
    this.loadGameButton.setMaxWidth(220);

    this.exitButton = new Button("Exit");
    this.exitButton.getStyleClass().add("start-button");
    this.exitButton.setMaxWidth(220);

    VBox actions = new VBox(12, this.newGameButton, this.loadGameButton, this.exitButton);
    actions.setAlignment(Pos.CENTER);

    this.errorLabel = new Label();
    this.errorLabel.getStyleClass().add("error-label");
    this.errorLabel.setVisible(false);
    this.errorLabel.setManaged(false);

    this.root = new VBox(24, title, subtitle, actions, this.errorLabel);
    this.root.setAlignment(Pos.CENTER);
    this.root.getStyleClass().add("start-view");
  }

  /**
   * Returns the root node so the application can attach it to a scene.
   *
   * @return the launcher root container
   */
  public VBox getRoot() {
    return this.root;
  }

  /**
   * Attaches the launcher stylesheet to the given scene.
   *
   * @param scene the scene to attach the stylesheet to
   */
  public void attachTo(Scene scene) {
    Objects.requireNonNull(scene, "Scene cannot be null");
    String css = Objects.requireNonNull(getClass().getResource(STYLESHEET),
        "Could not find stylesheet at " + STYLESHEET).toExternalForm();

    if (!scene.getStylesheets().contains(css)) {
      scene.getStylesheets().add(css);
    }
  }

  /**
   * Registers the action to run when the user clicks New Game.
   *
   * @param action the action to execute
   */
  public void setOnNewGame(Runnable action) {
    Objects.requireNonNull(action, "Action cannot be null");
    this.newGameButton.setOnAction(event -> action.run());
  }

  /**
   * Registers the action to run when the user clicks Load Saved Game.
   *
   * @param action the action to execute
   */
  public void setOnLoadGame(Runnable action) {
    Objects.requireNonNull(action, "Action cannot be null");
    this.loadGameButton.setOnAction(event -> action.run());
  }

  /**
   * Registers the action to run when the user clicks Exit.
   *
   * @param action the action to execute
   */
  public void setOnExit(Runnable action) {
    Objects.requireNonNull(action, "Action cannot be null");
    this.exitButton.setOnAction(event -> action.run());
  }

  /**
   * Displays an error message below the launcher actions.
   *
   * @param message the message to show, or {@code null} / empty to clear it
   */
  public void showError(String message) {
    if (message == null || message.isEmpty()) {
      this.errorLabel.setText("");
      this.errorLabel.setVisible(false);
      this.errorLabel.setManaged(false);
    } else {
      this.errorLabel.setText(message);
      this.errorLabel.setVisible(true);
      this.errorLabel.setManaged(true);
    }
  }
}
