package no.ntnu.idatt2003.group38.view.shell;

import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * The outer frame that wraps every page of the application:
 * A top bar showing player state sits at the
 * top, a side navigation sits on the left, and a center area has the
 * currently selected page.
 */
public class ShellView {

  private static final String STYLESHEET = "/stylesheets/shell.css";

  private final StackPane root;
  private final TopBar topBar;
  private final SideNav sideNav;
  private final BorderPane shellContent;
  private final StackPane modalLayer;

  /**
   * Builds a new shell with a fresh {@link TopBar} and {@link SideNav} and an
   * empty center content area.
   */
  public ShellView() {
    this.topBar = new TopBar();
    this.sideNav = new SideNav();

    this.shellContent = new BorderPane();
    this.shellContent.getStyleClass().add("shell");
    this.shellContent.setTop(this.topBar.getRoot());
    this.shellContent.setLeft(this.sideNav.getRoot());
    this.shellContent.setCenter(buildEmptyContent());

    this.modalLayer = new StackPane();
    this.modalLayer.getStyleClass().add("modal-layer");
    this.modalLayer.setVisible(false);
    this.modalLayer.setManaged(false);

    this.root = new StackPane(this.shellContent, this.modalLayer);
  }

  /**
   * Returns the root node of the shell so the application can attach it to a
   * {@link Scene}.
   *
   * @return the root layout container of the shell
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Returns the top bar component so a controller can wire it to model
   * observers and read its labels.
   *
   * @return the top bar
   */
  public TopBar getTopBar() {
    return this.topBar;
  }

  /**
   * Returns the side navigation component so a controller can wire its
   * navigation actions.
   *
   * @return the side navigation
   */
  public SideNav getSideNav() {
    return this.sideNav;
  }

  /**
   * Swaps the page currently shown in the center area.
   *
   * <p>The previous content is detached and replaced by {@code content}.
   *
   * @param content the new center content. Must not be {@code null}
   */
  public void setContent(Node content) {
    Objects.requireNonNull(content, "content cannot be null");
    this.shellContent.setCenter(content);
  }

  /**
   * Attaches shell stylesheet to given scene.
   *
   * @param scene the scene to attach the stylesheet to. Must not be {@code null}
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

  private Region buildEmptyContent() {
    Region placeholder = new Region();
    placeholder.getStyleClass().add("shell-content");
    return placeholder;
  }

  /**
   * Shows the given node as a modal overlay above the shell content.
   *
   * <p>While shown, the underlying shell receives no clicks. The overlay
   * dims the shell to focus attention on the modal content.
   *
   * @param content the modal content. Must not be {@code null}
   */
  public void showModal(Node content) {
    Objects.requireNonNull(content, "content cannot be null");
    this.modalLayer.getChildren().setAll(content);
    this.modalLayer.setVisible(true);
    this.modalLayer.setManaged(true);
  }

  /**
   * Hides the modal overlay and removes its content.
   */
  public void hideModal() {
    this.modalLayer.getChildren().clear();
    this.modalLayer.setVisible(false);
    this.modalLayer.setManaged(false);
  }
}