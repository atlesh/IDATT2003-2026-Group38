package no.ntnu.idatt2003.group38.view.components;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation popup
 *
 * <p>Designed to be shown inside the shell's modal layer via
 * {@code ShellView.showModal}.
 */
public class ConfirmDialog {

  private final VBox root;

  private Runnable onConfirm = () -> { };
  private Runnable onCancel = () -> { };

  /**
   * Builds a confirmation dialog.
   *
   * @param title the heading shown at the top of the dialog. Must not be {@code null}
   * @param message the body text explaining the action. Must not be {@code null}
   * @param confirmLabel the label for the confirm button (e.g. {@code "Advance"}).
   *                    Must not be {@code null}
   */
  public ConfirmDialog(String title, String message, String confirmLabel) {
    Objects.requireNonNull(title, "title cannot be null");
    Objects.requireNonNull(message, "message cannot be null");
    Objects.requireNonNull(confirmLabel, "confirmLabel cannot be null");

    Label header = new Label(title);
    header.getStyleClass().add("confirm-header");

    Label body = new Label(message);
    body.getStyleClass().add("confirm-message");
    body.setWrapText(true);

    Button cancelButton = new Button("Cancel");
    cancelButton.getStyleClass().add("confirm-cancel-button");
    cancelButton.setOnAction(e -> this.onCancel.run());

    Button confirmButton = new Button(confirmLabel);
    confirmButton.getStyleClass().add("confirm-confirm-button");
    confirmButton.setDefaultButton(true);
    confirmButton.setOnAction(e -> this.onConfirm.run());

    HBox buttons = new HBox(12, cancelButton, confirmButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    this.root = new VBox(16, header, body, buttons);
    this.root.setAlignment(Pos.TOP_LEFT);
    this.root.setMaxWidth(Region.USE_PREF_SIZE);
    this.root.setMaxHeight(Region.USE_PREF_SIZE);
    this.root.getStyleClass().add("confirm-dialog");
  }

  /**
   * Returns the root node so the dialog can be mounted as modal content.
   *
   * @return the root layout container of the dialog
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Registers the action to invoke when the user clicks Confirm.
   *
   * @param onConfirm the action. Must not be {@code null}
   */
  public void setOnConfirm(Runnable onConfirm) {
    this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm cannot be null");
  }

  /**
   * Registers the action to invoke when the user clicks Cancel.
   *
   * @param onCancel the action. Must not be {@code null}
   */
  public void setOnCancel(Runnable onCancel) {
    this.onCancel = Objects.requireNonNull(onCancel, "onCancel cannot be null");
  }
}
