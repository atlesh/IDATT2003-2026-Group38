package no.ntnu.idatt2003.group38.view.shell;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The left-hand navigation of the application shell.
 *
 * <p>Hosts one button per page.
 */
public class SideNav {

  /**
   * Pages reachable from the side navigation.
   */
  public enum Destination {
    /**
     * Opens the dashboard page.
     */
    DASHBOARD("Dashboard"),
    /**
     * Opens the market page.
     */
    MARKET("Market"),
    /**
     * Opens the portfolio page.
     */
    PORTFOLIO("Portfolio"),
    /**
     * Opens the transaction-history page.
     */
    TRANSACTIONS("Transactions"),
    /**
     * Opens the weekly-statistics page.
     */
    WEEKLY_STATISTICS("Weekly Statistics");

    private final String label;

    Destination(String label) {
      this.label = label;
    }

    /**
     * Returns the readable label shown on the navigation button.
     *
     * @return the label
     */
    public String getLabel() {
      return this.label;
    }
  }

  private final VBox root;
  private final Map<Destination, Button> buttons;

  private Consumer<Destination> onNavigate = destination -> {
  };
  private Destination activeDestination;

  /**
   * Builds the side navigation with one button per {@link Destination}.
   * The first destination ({@link Destination#DASHBOARD}) is marked active
   * by default.
   */
  public SideNav() {
    this.buttons = new EnumMap<>(Destination.class);

    this.root = new VBox(8);
    this.root.setAlignment(Pos.TOP_CENTER);
    this.root.getStyleClass().add("side-nav");

    for (Destination destination : Destination.values()) {
      Button button = buildNavButton(destination);
      this.buttons.put(destination, button);
      this.root.getChildren().add(button);
    }

    setActive(Destination.DASHBOARD);
  }

  /**
   * Returns the root node so the shell can mount the side nav.
   *
   * @return the root layout container of the side nav
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Registers the callback to invoke when the user clicks a navigation button.
   *
   * @param onNavigate the callback to invoke; must not be {@code null}
   */
  public void setOnNavigate(Consumer<Destination> onNavigate) {
    this.onNavigate = Objects.requireNonNull(onNavigate, "onNavigate cannot be null");
  }

  /**
   * Marks the given destination as the active one and removes the active
   * styling from all other buttons.
   *
   * @param destination the destination to mark active; must not be {@code null}
   */
  public void setActive(Destination destination) {
    Objects.requireNonNull(destination, "destination cannot be null");
    if (destination == this.activeDestination) {
      return;
    }
    this.activeDestination = destination;
    this.buttons.forEach((dest, button) -> {
      if (dest == destination) {
        if (!button.getStyleClass().contains("active")) {
          button.getStyleClass().add("active");
        }
      } else {
        button.getStyleClass().remove("active");
      }
    });
  }

  /**
   * Returns the currently active destination.
   *
   * @return the active destination, never {@code null} after construction
   */
  public Destination getActive() {
    return this.activeDestination;
  }

  /**
   * Builds a single navigation button wired to the navigation callback.
   *
   * @param destination the destination this button targets
   * @return the configured button
   */
  private Button buildNavButton(Destination destination) {
    Button button = new Button(destination.getLabel());
    button.getStyleClass().add("side-nav-button");
    button.setMaxWidth(Double.MAX_VALUE);
    button.setOnAction(e -> this.onNavigate.accept(destination));
    return button;
  }
}
