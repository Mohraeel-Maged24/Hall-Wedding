package views;

import hall_wedding.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

public class WelcomeView {

    private static final String HERO_IMAGE_PATH = "/viewIcon/Gemini_Generated_Image_9t6f4o9t6f4o9t6f.png";
    private static final String[] DYNAMIC_HERO_URLS = {
        "https://images.unsplash.com/photo-1519225421980-715cb0215aed?auto=format&fit=crop&w=1600&q=80",
        "https://images.unsplash.com/photo-1522673607200-164d1b6ce486?auto=format&fit=crop&w=1600&q=80",
        "https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?auto=format&fit=crop&w=1600&q=80",
        "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=1600&q=80"
    };

    public static Scene build() {
        StackPane root = new StackPane();
        root.setStyle(Theme.appBackgroundStyle());
        root.setPrefSize(980, 620);

        ImageView hero = new ImageView(loadDynamicHeroImage());
        hero.setFitWidth(980);
        hero.setFitHeight(620);
        hero.setPreserveRatio(false);

        Rectangle overlay = new Rectangle(980, 620);
        overlay.setStyle("-fx-fill: linear-gradient(to bottom, rgba(26,18,15,0.3), rgba(26,18,15,0.74));");

        hero.fitWidthProperty().bind(root.widthProperty());
        hero.fitHeightProperty().bind(root.heightProperty());
        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());

        // Content
        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(70, 60, 70, 60));

        Label word = label("Welcome", "-fx-font-size:68px; -fx-font-family:'Palatino Linotype'; -fx-text-fill:#F7F0E4; -fx-font-weight:bold;");
        Label title = label("Wedding Hall System", Theme.titleStyle(40));
        Label tagline = label("Where your forever begins in elegance",
                              "-fx-font-size:17px; -fx-text-fill:" + Theme.CREAM + "; -fx-font-family:'Georgia';");

        Separator sep = new Separator();
        sep.setMaxWidth(260);
        sep.setStyle("-fx-background-color:" + Theme.GOLD + ";");

        Button startBtn = new Button("Enter The Venue");
        startBtn.setStyle(Theme.goldButtonStyle());
        startBtn.setOnAction(e -> SceneManager.switchTo("login"));
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
            Theme.goldButtonStyle().replace(Theme.GOLD, Theme.GOLD_LIGHT)));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(Theme.goldButtonStyle()));

        Label footer = label("A Grand Experience For A Grand Day",
                             "-fx-font-size:11px; -fx-text-fill:" + Theme.CREAM_DIM + ";");

        content.getChildren().addAll(word, title, tagline, sep, startBtn, footer);
        root.getChildren().addAll(hero, overlay, content);
        return new Scene(root, 980, 620);
    }

    private static Label label(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

    private static Image loadDynamicHeroImage() {
        String url = DYNAMIC_HERO_URLS[ThreadLocalRandom.current().nextInt(DYNAMIC_HERO_URLS.length)];
        try {
            return new Image(url, true);
        } catch (Exception ignored) {
            // Fall back to bundled image if the remote image cannot be loaded.
        }

        InputStream in = WelcomeView.class.getResourceAsStream(HERO_IMAGE_PATH);
        if (in == null) {
            return new Image("https://images.unsplash.com/photo-1519225421980-715cb0215aed?auto=format&fit=crop&w=1600&q=80", true);
        }
        return new Image(in);
    }
}