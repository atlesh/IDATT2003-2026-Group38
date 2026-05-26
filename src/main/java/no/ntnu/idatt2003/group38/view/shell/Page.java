package no.ntnu.idatt2003.group38.view.shell;

import javafx.scene.layout.Region;

/**
 * A swappable page that can be mounted in the {@link ShellView}'s center area.
 */
public interface Page {

  /**
   * Returns the root node of the page so the shell can mount it.
   *
   * @return the root node, never {@code null}
   */
  Region getRoot();

  /**
   * Called when the page is mounted in the shell.
   */
  default void onAttach() {
  }

  /**
   * Called when the page is unmounted from the shell.
   */
  default void onDetach() {
  }
}