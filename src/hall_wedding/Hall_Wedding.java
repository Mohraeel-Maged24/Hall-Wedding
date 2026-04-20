package hall_wedding;

import javafx.application.Application;
import javafx.stage.Stage;

public class Hall_Wedding extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        DBSeeder.seedIfEmpty();
        primaryStage = stage;
        stage.setTitle("💍 Wedding Hall System");
        stage.setResizable(false);
        SceneManager.switchTo("welcome");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}