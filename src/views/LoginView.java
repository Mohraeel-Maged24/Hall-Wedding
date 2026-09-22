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

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35, 60, 35, 60));
        card.setMaxWidth(550);
        card.setMaxHeight(380);
        card.setStyle(Theme.cardStyle());

        Label icon  = new Label("💍");
        icon.setStyle("-fx-font-size:40px;");
        Label title = new Label("Welcome Back");
        title.setStyle(Theme.titleStyle(26));
        Label sub   = new Label("Sign in to start your wedding journey");
        sub.setStyle(Theme.subtitleStyle());

        TextField     emailFld = inputField("Email address");
        emailFld.setMaxWidth(420);
        emailFld.setMinHeight(38);
        
        PasswordField passFld  = new PasswordField();
        passFld.setPromptText("Password");
        passFld.setStyle(Theme.inputStyle());
        passFld.setMaxWidth(420);
        passFld.setMinHeight(38);

        Label emailErr = errorLabel();
        Label passErr = errorLabel();

        Button loginBtn = new Button("Customer Login");
        loginBtn.setStyle(primaryButtonStyle());
        loginBtn.setMaxWidth(420);
        loginBtn.setMinHeight(48);

        Button adminBtn = new Button("Admin Login");
        adminBtn.setStyle(secondaryButtonStyle());
        adminBtn.setMaxWidth(420);
        adminBtn.setMinHeight(48);

        Button forgotBtn = new Button("Forgot Password?");
        forgotBtn.setStyle(textButtonStyle());
        forgotBtn.setMaxWidth(420);

        Button backBtn = new Button("← Back to Welcome");
        backBtn.setStyle(backButtonStyle());
        backBtn.setMaxWidth(150);

        // ── Actions: View يستدعي Controller فقط ─────────
        loginBtn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass = passFld.getText().trim();
            clearErrors(emailErr, passErr);
            if (email.isEmpty()) { showError(emailErr, "Email is required"); return; }
            if (!email.matches(EMAIL_REGEX)) { showError(emailErr, "Invalid email format"); return; }
            if (pass.isEmpty()) { showError(passErr, "Password is required"); return; }
            if (pass.length() < 4) { showError(passErr, "Password must be at least 4 characters"); return; }

            boolean ok = LoginController.loginAsCustomer(email, pass);
            if (ok) SceneManager.switchTo("my-bookings");
            else    showError(passErr, "Invalid email or password");
        });

        adminBtn.setOnAction(e -> {
            String email = emailFld.getText().trim();
            String pass = passFld.getText().trim();
            clearErrors(emailErr, passErr);
            if (email.isEmpty()) { showError(emailErr, "Email is required"); return; }
            if (!email.matches(EMAIL_REGEX)) { showError(emailErr, "Invalid email format"); return; }
            if (pass.isEmpty()) { showError(passErr, "Password is required"); return; }
            if (pass.length() < 4) { showError(passErr, "Password must be at least 4 characters"); return; }

            boolean ok = LoginController.loginAsAdmin(email, pass);
            if (ok) SceneManager.switchTo("admin");
            else    showError(passErr, "Admin not found");
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

            Label forgotEmailErr = errorLabel();
            Label newPassErr = errorLabel();
            Label confirmErr = errorLabel();

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            grid.addRow(0, new Label("Email"), forgotEmail);
            grid.add(forgotEmailErr, 1, 1);
            grid.addRow(2, new Label("New Password"), newPassword);
            grid.add(newPassErr, 1, 3);
            grid.addRow(4, new Label("Confirm Password"), confirmPassword);
            grid.add(confirmErr, 1, 5);

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
                        showError(passErr, "✅ Password updated for " + updated + " account(s)");
                    } catch (Exception ex) {
                        showError(passErr, ex.getMessage());
                    }
                }
            });
        });

        backBtn.setOnAction(e -> SceneManager.switchTo("welcome"));

        card.getChildren().addAll(icon, title, sub,
            labeledWithError("Email", emailFld, emailErr),
            labeledWithError("Password", passFld, passErr),
            loginBtn, adminBtn, forgotBtn, backBtn);

        root.getChildren().add(card);
        return new Scene(root, 820, 540);
    }

    private static TextField inputField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(Theme.inputStyle());
        f.setMaxWidth(420);
        f.setMinHeight(38);
        return f;
    }

    private static VBox labeled(String title, Node field) {
        Label label = new Label(title);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        return new VBox(4, label, field);
    }

    private static VBox labeledWithError(String title, Node field, Label errorLabel) {
        Label label = new Label(title);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        return new VBox(3, label, field, errorLabel);
    }

    private static Label errorLabel() {
        Label label = new Label("");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill:" + Theme.ERROR + "; -fx-font-size:12px;");
        return label;
    }

    private static void clearErrors(Label... labels) {
        for (Label label : labels) label.setText("");
    }

    private static void showError(Label label, String msg) {
        label.setStyle("-fx-text-fill:" + Theme.ERROR + "; -fx-font-size:12px;");
        label.setText(msg);
    }

    private static String primaryButtonStyle() {
        return "-fx-background-color:" + Theme.GOLD + ";" +
               "-fx-text-fill:#FFFFFF;" +
               "-fx-font-size:14px;" +
               "-fx-font-weight:bold;" +
               "-fx-padding:12 40;" +
               "-fx-background-radius:10;" +
               "-fx-border-color: rgba(184,137,59,0.4);" +
               "-fx-border-width:1;" +
               "-fx-border-radius:10;" +
               "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.2), 12, 0.25, 0, 4);" +
               "-fx-cursor:hand;" +
               "-fx-text-alignment:CENTER;";
    }

    private static String secondaryButtonStyle() {
        return "-fx-background-color:" + Theme.ROSE + ";" +
               "-fx-text-fill:#FFFFFF;" +
               "-fx-font-size:14px;" +
               "-fx-font-weight:bold;" +
               "-fx-padding:12 40;" +
               "-fx-background-radius:10;" +
               "-fx-border-color: rgba(200,146,134,0.4);" +
               "-fx-border-width:1;" +
               "-fx-border-radius:10;" +
               "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.15), 10, 0.2, 0, 3);" +
               "-fx-cursor:hand;" +
               "-fx-text-alignment:CENTER;";
    }

    private static String textButtonStyle() {
        return "-fx-background-color:transparent;" +
               "-fx-text-fill:" + Theme.GOLD + ";" +
               "-fx-font-size:13px;" +
               "-fx-font-weight:600;" +
               "-fx-padding:8 0;" +
               "-fx-border-color:transparent;" +
               "-fx-cursor:hand;" +
               "-fx-underline:false;";
    }

    private static String backButtonStyle() {
        return "-fx-background-color:" + Theme.GOLD_LIGHT + ";" +
               "-fx-text-fill:#FFFFFF;" +
               "-fx-font-size:12px;" +
               "-fx-font-weight:600;" +
               "-fx-padding:8 20;" +
               "-fx-background-radius:8;" +
               "-fx-border-color: rgba(211,168,94,0.3);" +
               "-fx-border-width:1;" +
               "-fx-border-radius:8;" +
               "-fx-cursor:hand;" +
               "-fx-text-alignment:CENTER;";
    }
}