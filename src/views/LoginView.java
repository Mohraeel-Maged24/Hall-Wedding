package views;

import controllers.LoginController;
import hall_wedding.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.*;

public class LoginView {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

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
        errLabel.setWrapText(true);
        errLabel.setMaxWidth(Double.MAX_VALUE);
        errLabel.setPrefWidth(320);

        Button loginBtn = new Button("Customer Login");
        loginBtn.setStyle(Theme.goldButtonStyle());
        loginBtn.setMaxWidth(300);

        Button adminBtn = new Button("Admin Login");
        adminBtn.setStyle(Theme.roseButtonStyle());
        adminBtn.setMaxWidth(300);

        Button forgotBtn = new Button("Forgot Password");
        forgotBtn.setStyle(Theme.navButtonStyle());
        forgotBtn.setMaxWidth(300);

        Button backBtn = new Button("Back");
        backBtn.setStyle(Theme.navButtonStyle());

        // ── Actions: View يستدعي Controller فقط ─────────
        loginBtn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass = passFld.getText().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                errLabel.setText("⚠️ Email and password are required");
                return;
            }
            if (!email.matches(EMAIL_REGEX)) {
                errLabel.setText("⚠️ Invalid email format");
                return;
            }
            if (pass.length() < 4) {
                errLabel.setText("⚠️ Password must be at least 4 characters");
                return;
            }

            boolean ok = LoginController.loginAsCustomer(email, pass);
            if (ok) SceneManager.switchTo("my-bookings");
            else    errLabel.setText("❌ Invalid email or password");
        });

        adminBtn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass = passFld.getText().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                errLabel.setText("⚠️ Email and password are required");
                return;
            }
            if (!email.matches(EMAIL_REGEX)) {
                errLabel.setText("⚠️ Invalid email format");
                return;
            }
            if (pass.length() < 4) {
                errLabel.setText("⚠️ Password must be at least 4 characters");
                return;
            }

            boolean ok = LoginController.loginAsAdmin(email, pass);
            if (ok) SceneManager.switchTo("admin");
            else    errLabel.setText("❌ Admin not found");
        });

        forgotBtn.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Forgot Password");
            dialog.setHeaderText("Reset your password using your email");

            ButtonType resetType = new ButtonType("Reset", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(resetType, ButtonType.CANCEL);

            TextField forgotEmail = new TextField();
            forgotEmail.setPromptText("Email");
            PasswordField newPassword = new PasswordField();
            newPassword.setPromptText("New Password");
            PasswordField confirmPassword = new PasswordField();
            confirmPassword.setPromptText("Confirm Password");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            grid.addRow(0, new Label("Email"), forgotEmail);
            grid.addRow(1, new Label("New Password"), newPassword);
            grid.addRow(2, new Label("Confirm Password"), confirmPassword);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().lookupButton(resetType).setDisable(true);

            Runnable validate = () -> {
                boolean valid = !forgotEmail.getText().trim().isEmpty()
                    && forgotEmail.getText().trim().matches(EMAIL_REGEX)
                    && newPassword.getText().trim().length() >= 4
                    && newPassword.getText().equals(confirmPassword.getText());
                dialog.getDialogPane().lookupButton(resetType).setDisable(!valid);
            };

            forgotEmail.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
            newPassword.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
            confirmPassword.textProperty().addListener((obs, oldValue, newValue) -> validate.run());

            dialog.setResultConverter(button -> button == resetType ? resetType : ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(result -> {
                if (result == resetType) {
                    try {
                        int updated = LoginController.resetPasswordByEmail(
                            forgotEmail.getText().trim(),
                            newPassword.getText().trim());
                        errLabel.setText("✅ Password updated for " + updated + " account(s)");
                    } catch (Exception ex) {
                        errLabel.setText("❌ " + ex.getMessage());
                    }
                }
            });
        });

        backBtn.setOnAction(e -> SceneManager.switchTo("welcome"));

        card.getChildren().addAll(icon, title, sub,
            labeled("Email", emailFld),
            labeled("Password", passFld),
            errLabel, loginBtn, adminBtn, forgotBtn, backBtn);

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

    private static VBox labeled(String title, Node field) {
        Label label = new Label(title);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        return new VBox(4, label, field);
    }
}