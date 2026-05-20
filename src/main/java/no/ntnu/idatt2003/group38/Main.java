package no.ntnu.idatt2003.group38;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import no.ntnu.idatt2003.group38.controller.LauncherController;
import no.ntnu.idatt2003.group38.view.LauncherView;

/**
 * Entry point of the Millions application.
 *
 * <p>Loads the Roboto font family from {@code src/main/resources/fonts}
 * and shows the {@link LauncherView} as the initial scene.
 */
public class Main extends Application {

  @Override
  public void start(Stage stage) {
    loadFonts();

    LauncherView launcherView = new LauncherView();
    new LauncherController(launcherView, stage);

    Scene scene = new Scene(launcherView.getRoot(), 760, 480);
    launcherView.attachTo(scene);

    stage.setTitle("Millions");
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Loads the bundled Roboto font files so the view can render in Roboto
   * regardless of which fonts are installed on the host system.
   */
  private void loadFonts() {
    Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Light.ttf"), 10);
    Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf"), 10);
    Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Bold.ttf"), 10);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
