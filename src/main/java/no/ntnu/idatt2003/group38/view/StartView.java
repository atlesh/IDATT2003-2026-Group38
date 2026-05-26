package no.ntnu.idatt2003.group38.view;

import java.io.File;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The start view of the Millions application.
 *
 * <p>Lets the user choose a username, enter a starting capital and pick a
 * stock data file before starting a new game.</p>
 *
 * <p>All visual styling is delegated to {@code stylesheets/start-view.css}.</p>
 *
 * <p>The view does not perform any input validation or business logic.
 * The controller is responsible for reading values via {@link #getUsername()},
 * {@link #getStartingCapital()} and {@link #getStockFile()}, validating them,
 * and reporting problems through {@link #showError(String)}.</p>
 */
public class StartView {

  private static final String STYLESHEET = "/stylesheets/start-view.css";
  private static final double FIELD_WIDTH = 320;

  private final VBox root;
  private final TextField usernameField;
  private final TextField capitalField;
  private final TextField fileField;
  private final Button pickFileButton;
  private final Button startGameButton;
  private final Label errorLabel;

  private File selectedFile;

  /**
   * Builds the start view and all its components.
   */
  public StartView() {
    Label title = new Label("MILLIONS");
    title.getStyleClass().add("start-view-title");

    this.usernameField = new TextField();
    this.usernameField.setPromptText("Username");
    VBox usernameGroup = buildField("Choose Username", this.usernameField);

    this.capitalField = new TextField();
    this.capitalField.setPromptText("0.0");
    VBox capitalGroup = buildField("Starting Capital", this.capitalField);

    this.fileField = new TextField();
    this.fileField.setEditable(false);
    this.fileField.setPromptText("No File Picked Yet");
    this.fileField.getStyleClass().add("start-view-field");

    this.pickFileButton = new Button("…"); // ellipsis character
    this.pickFileButton.getStyleClass().add("pick-file-button");

    HBox fileRow = new HBox(8, this.pickFileButton, this.fileField);
    fileRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(this.fileField, Priority.ALWAYS);

    Label fileLabel = new Label("Pick Stock-file");
    fileLabel.getStyleClass().add("field-label");

    VBox fileGroup = new VBox(6, fileLabel, fileRow);
    fileGroup.setMaxWidth(FIELD_WIDTH);

    this.startGameButton = new Button("Start Game");
    this.startGameButton.getStyleClass().add("start-button");

    this.errorLabel = new Label();
    this.errorLabel.getStyleClass().add("error-label");
    this.errorLabel.setVisible(false);
    this.errorLabel.setManaged(false);

    VBox form = new VBox(18, usernameGroup, capitalGroup, fileGroup);
    form.setAlignment(Pos.CENTER);

    this.root = new VBox(28, title, form, this.startGameButton, this.errorLabel);
    this.root.setAlignment(Pos.CENTER);
    this.root.getStyleClass().add("start-view");
  }

  /**
   * Returns the root node so the application can attach it to a {@link Scene}.
   *
   * @return the root layout of the start view
   */
  public VBox getRoot() {
    return this.root;
  }

  /**
   * Attaches this view's stylesheet to the given scene.
   *
   * @param scene the scene to attach the stylesheet to; must not be {@code null}
   * @throws NullPointerException if {@code scene} is {@code null} or if the
   *                              stylesheet resource cannot be found on the classpath
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

  // Component construction

  private VBox buildField(String labelText, TextField field) {
    Label label = new Label(labelText);
    label.getStyleClass().add("field-label");

    field.setMaxWidth(FIELD_WIDTH);
    field.getStyleClass().add("start-view-field");

    VBox box = new VBox(6, label, field);
    box.setMaxWidth(FIELD_WIDTH);
    return box;
  }

  // Controller hooks

  /**
   * Registers the action to run when the user clicks the "Start Game" button.
   *
   * @param action the action to execute
   */
  public void setOnStartGame(Runnable action) {
    this.startGameButton.setOnAction(e -> action.run());
  }

  /**
   * Registers the action to run when the user clicks the file picker button.
   *
   * <p>The action is typically responsible for opening a {@code FileChooser}
   * and then calling {@link #setSelectedFile(File)} with the chosen file.
   *
   * @param action the action to execute
   */
  public void setOnPickFile(Runnable action) {
    this.pickFileButton.setOnAction(e -> action.run());
  }

  // Values

  /**
   * Returns the username currently entered by the user.
   *
   * @return the trimmed username text, or an empty string if none has been entered
   */
  public String getUsername() {
    return this.usernameField.getText() == null ? "" : this.usernameField.getText().trim();
  }

  /**
   * Returns the starting capital text currently entered by the user.
   *
   * @return the trimmed starting capital text, or an empty string if none has been entered
   */
  public String getStartingCapital() {
    return this.capitalField.getText() == null ? "" : this.capitalField.getText().trim();
  }

  /**
   * Returns the stock file currently selected by the user.
   *
   * @return the selected {@link File}, or {@code null} if none has been selected
   */
  public File getStockFile() {
    return this.selectedFile;
  }

  /**
   * Updates the view to reflect the file the user has selected.
   *
   * @param file the selected file, or {@code null} to clear the selection
   */
  public void setSelectedFile(File file) {
    this.selectedFile = file;
    this.fileField.setText(file == null ? "" : file.getName());
  }

  // Feedback

  /**
   * Displays an error message below the form.
   *
   * @param message the message to show. If {@code null} or empty the error is cleared
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