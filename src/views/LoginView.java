package views;

import controllers.LoginController;
import hall_wedding.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView {

    public static Scene build() {
        StackPane root = new StackPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(820, 540);

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(45, 50, 45, 50));
        card.setMaxWidth(380);
        card.setStyle(Theme.cardStyle());

        Label icon  = new Label("💍");
        icon.setStyle("-fx-font-size:40px;");
        Label title = new Label("Welcome Back");
        title.setStyle(Theme.titleStyle(26));
        Label sub   = new Label("Sign in to start your wedding journey");
        sub.setStyle(Theme.subtitleStyle());

        TextField     emailFld = inputField("Email address");
        PasswordField passFld  = new PasswordField();
        passFld.setPromptText("Password");
        passFld.setStyle(Theme.inputStyle());
        passFld.setMaxWidth(300);

        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill:" + Theme.ERROR + "; -fx-font-size:12px;");

        Button loginBtn = new Button("Login as Customer");
        loginBtn.setStyle(Theme.goldButtonStyle());
        loginBtn.setMaxWidth(300);

        Button adminBtn = new Button("Login as Admin");
        adminBtn.setStyle(Theme.roseButtonStyle());
        adminBtn.setMaxWidth(300);

        Button backBtn = new Button("← Back");
        backBtn.setStyle(Theme.navButtonStyle());

        // ── Actions: View يستدعي Controller فقط ─────────
        loginBtn.setOnAction(e -> {
            boolean ok = LoginController.loginAsCustomer(
                emailFld.getText().trim(), passFld.getText().trim());
            if (ok) SceneManager.switchTo("my-bookings");
            else    errLabel.setText("❌ Invalid email or password");
        });

        adminBtn.setOnAction(e -> {
            boolean ok = LoginController.loginAsAdmin(
                emailFld.getText().trim(), passFld.getText().trim());
            if (ok) SceneManager.switchTo("admin");
            else    errLabel.setText("❌ Admin not found");
        });

        backBtn.setOnAction(e -> SceneManager.switchTo("welcome"));

        card.getChildren().addAll(icon, title, sub,
            emailFld, passFld, errLabel, loginBtn, adminBtn, backBtn);

        root.getChildren().add(card);
        return new Scene(root, 820, 540);
    }

    private static TextField inputField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(Theme.inputStyle());
        f.setMaxWidth(300);
        return f;
    }
}