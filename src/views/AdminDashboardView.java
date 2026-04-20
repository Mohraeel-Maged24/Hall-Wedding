package views;

import controllers.AdminController;
import hall_wedding.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminDashboardView {

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(900, 580);

        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());
        return new Scene(root, 900, 580);
    }

    // ── Sidebar ──────────────────────────────────────────
    private static VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(30, 16, 30, 16));
        sidebar.setPrefWidth(210);
        sidebar.setStyle(Theme.frostedPanelStyle());

        Label logo = new Label("💍  Admin Panel");
        logo.setStyle(Theme.titleStyle(17));
        logo.setPadding(new Insets(0, 0, 16, 4));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:" + Theme.GOLD + ";");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = navBtn("⬅   Logout", "login");
        logoutBtn.setOnAction(e -> {
            controllers.LoginController.logout();        // ← Controller يعمل logout
            SceneManager.switchTo("login");
        });

        sidebar.getChildren().addAll(
            logo, sep,
            navBtn("🏛️   Halls",     "halls"),
            navBtn("🛎️   Services",  "services"),
            navBtn("👥   Customers", "customers"),
            navBtn("📋   Bookings",  "bookings"),
            spacer,
            logoutBtn
        );
        return sidebar;
    }

    // ── Center content ───────────────────────────────────
    private static VBox buildCenter() {
        VBox center = new VBox(24);
        center.setPadding(new Insets(40));
        center.setAlignment(Pos.TOP_LEFT);

        Label welcomeTitle = new Label("Welcome, Admin 👋");
        welcomeTitle.setStyle(Theme.titleStyle(28));

        Label welcomeSub = new Label("Here's an overview of your wedding hall system");
        welcomeSub.setStyle(Theme.subtitleStyle());

        // Controller يجيب الأرقام، View تعرضها فقط
        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            statCard("🏛️", AdminController.hallCount(),     "Halls"),
            statCard("🛎️", AdminController.serviceCount(),  "Services"),
            statCard("📋", AdminController.bookingCount(),  "Bookings"),
            statCard("👥", AdminController.customerCount(), "Customers")
        );

        center.getChildren().addAll(welcomeTitle, welcomeSub, statsRow);
        return center;
    }

    private static Button navBtn(String text, String page) {
        Button btn = new Button(text);
        btn.setStyle(Theme.navButtonStyle());
        btn.setPrefWidth(178);
        btn.setOnAction(e -> SceneManager.switchTo(page));
        btn.setOnMouseEntered(ev -> btn.setStyle(Theme.navButtonActiveStyle()));
        btn.setOnMouseExited(ev  -> btn.setStyle(Theme.navButtonStyle()));
        return btn;
    }

    private static VBox statCard(String icon, long count, String label) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 28, 20, 28));
        card.setStyle(Theme.cardStyle());
        Label iconLbl  = new Label(icon);  iconLbl.setStyle("-fx-font-size:26px;");
        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" + Theme.GOLD + ";");
        Label nameLbl  = new Label(label); nameLbl.setStyle(Theme.subtitleStyle());
        card.getChildren().addAll(iconLbl, countLbl, nameLbl);
        return card;
    }
}