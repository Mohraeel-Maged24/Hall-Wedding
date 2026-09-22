package views;

import controllers.BookingController;
import controllers.CustomerController;
import controllers.HallController;
import controllers.ServiceController;
import hall_wedding.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminDashboardView {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    public static Scene build() {
        StackPane root = new StackPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(960, 700);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(16));
        layout.setCenter(buildDashboardShell());

        root.getChildren().addAll(buildBackdrop(), layout);
        return new Scene(root, 960, 700);
    }

    private static Pane buildBackdrop() {
        Pane backdrop = new Pane();
        backdrop.setMouseTransparent(true);
        backdrop.setPickOnBounds(false);

        Circle topLeft = floatingCircle(150, Color.web(Theme.GOLD_LIGHT, 0.12), -34, -20);
        Circle topRight = floatingCircle(110, Color.web(Theme.ROSE_LIGHT, 0.14), 760, 30);
        Circle bottomLeft = floatingCircle(180, Color.web(Theme.GOLD, 0.08), 80, 510);
        Circle bottomRight = floatingCircle(135, Color.web(Theme.ROSE, 0.10), 800, 470);

        backdrop.getChildren().addAll(topLeft, topRight, bottomLeft, bottomRight);

        drift(topLeft, -10, 14, 5200, 0);
        drift(topRight, 12, -12, 6400, 180);
        drift(bottomLeft, 14, -8, 5800, 320);
        drift(bottomRight, -10, 10, 7000, 500);

        return backdrop;
    }

    private static Circle floatingCircle(double radius, Color fill, double x, double y) {
        Circle circle = new Circle(radius, fill);
        circle.setCenterX(x + radius);
        circle.setCenterY(y + radius);
        circle.setStroke(Color.web("rgba(255,255,255,0.35)"));
        circle.setStrokeWidth(1.2);
        return circle;
    }

    private static void drift(Circle circle, double dx, double dy, double durationMs, double delayMs) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(durationMs), circle);
        transition.setByX(dx);
        transition.setByY(dy);
        transition.setAutoReverse(true);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.setDelay(Duration.millis(delayMs));
        transition.play();
    }

    private static VBox buildDashboardShell() {
        StringProperty searchQuery = new javafx.beans.property.SimpleStringProperty("");

        VBox shell = new VBox();
        shell.setSpacing(18);
        shell.setPadding(new Insets(0));

        HBox content = new HBox(18);
        content.getChildren().addAll(buildSidebar(), buildMainContent(searchQuery));
        HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

        shell.getChildren().add(content);
        return shell;
    }

    private static VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(214);
        sidebar.setMinWidth(214);
        sidebar.setStyle(Theme.frostedPanelStyle() + "-fx-background-radius:24; -fx-border-radius:24;");

        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);

        StackPane brandIcon = new StackPane();
        brandIcon.setPrefSize(30, 30);
        brandIcon.setStyle("-fx-background-color: rgba(255,255,255,0.45); -fx-background-radius:10; -fx-border-color: rgba(193,221,255,0.55); -fx-border-radius:10; -fx-border-width:1;");
        Label brandLetter = new Label("O");
        brandLetter.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#6E7ED8;");
        brandIcon.getChildren().add(brandLetter);

        VBox brandText = new VBox(2);
        Label brandTitle = new Label("Hall");
        brandTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#1F2A44;");
        Label brandSub = new Label("MANAGEMENT");
        brandSub.setStyle("-fx-font-size:10px; -fx-text-fill:#6E7ED8;");
        brandText.getChildren().addAll(brandTitle, brandSub);

        brand.getChildren().addAll(brandIcon, brandText);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(193,221,255,0.75);");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
            brand,
            sep,
            navButton("⌂  Overview", true),
            navButton("🏛  Halls", false),
            navButton("🛎  Services", false),
            navButton("👥  Customers", false),
            navButton("📋  Bookings", false),
            spacer,
            logoutButton()
        );

        return sidebar;
    }

    private static Button navButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(active ? activeNavStyle() : glassNavStyle());
        btn.setOnMousePressed(e -> {
            if (!active) btn.setStyle(activeNavStyle());
            btn.setTranslateX(3);
        });
        btn.setOnMouseReleased(e -> {
            btn.setStyle(active ? activeNavStyle() : glassNavStyle());
            btn.setTranslateX(0);
        });
        btn.setOnAction(e -> {
            String target = text.contains("Halls") ? "halls"
                    : text.contains("Services") ? "services"
                    : text.contains("Customers") ? "customers"
                    : text.contains("Bookings") ? "bookings"
                    : null;
            if (target != null) {
                PauseTransition pause = new PauseTransition(Duration.millis(120));
                pause.setOnFinished(ev -> SceneManager.switchTo(target));
                pause.play();
            }
        });
        btn.setOnMouseEntered(e -> {
            if (!active) btn.setStyle(glassNavHoverStyle());
            btn.setTranslateX(3);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(active ? activeNavStyle() : glassNavStyle());
            btn.setTranslateX(0);
        });
        return btn;
    }

    private static Button logoutButton() {
        Button btn = new Button("Logout");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(glassNavStyle());
        btn.setOnMousePressed(e -> btn.setStyle(glassNavHoverStyle()));
        btn.setOnMouseReleased(e -> btn.setStyle(glassNavStyle()));
        btn.setOnAction(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(120));
            pause.setOnFinished(ev -> {
                controllers.LoginController.logout();
                SceneManager.switchTo("login");
            });
            pause.play();
        });
        btn.setOnMouseEntered(e -> btn.setStyle(glassNavHoverStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(glassNavStyle()));
        return btn;
    }

    private static String activeNavStyle() {
        return "-fx-background-color: rgba(255,255,255,0.72);" +
               "-fx-text-fill:#B8893B;" +
               "-fx-font-size:14px;" +
               "-fx-font-weight:bold;" +
               "-fx-font-family:'Georgia';" +
               "-fx-alignment:CENTER_LEFT;" +
               "-fx-padding:10 16;" +
               "-fx-background-radius:20;" +
               "-fx-border-color: rgba(147,194,255,0.95);" +
               "-fx-border-width:1.2;" +
               "-fx-border-radius:20;" +
               "-fx-effect: dropshadow(gaussian, rgba(147,194,255,0.22), 10, 0.2, 0, 3);" +
               "-fx-cursor:hand;";
    }

    private static String glassNavStyle() {
        return "-fx-background-color: rgba(255,255,255,0.44);" +
               "-fx-text-fill:#B8893B;" +
               "-fx-font-size:14px;" +
               "-fx-font-weight:bold;" +
               "-fx-font-family:'Georgia';" +
               "-fx-alignment:CENTER_LEFT;" +
               "-fx-padding:10 16;" +
               "-fx-background-radius:20;" +
               "-fx-border-color: rgba(193,221,255,0.65);" +
               "-fx-border-width:1;" +
               "-fx-border-radius:20;" +
               "-fx-effect: dropshadow(gaussian, rgba(128,167,215,0.12), 10, 0.18, 0, 3);" +
               "-fx-cursor:hand;";
    }

    private static String glassNavHoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.64);" +
               "-fx-text-fill:#B8893B;" +
               "-fx-font-size:14px;" +
               "-fx-font-weight:bold;" +
               "-fx-font-family:'Georgia';" +
               "-fx-alignment:CENTER_LEFT;" +
               "-fx-padding:10 16;" +
               "-fx-background-radius:20;" +
               "-fx-border-color: rgba(147,194,255,0.95);" +
               "-fx-border-width:1.15;" +
               "-fx-border-radius:20;" +
               "-fx-effect: dropshadow(gaussian, rgba(147,194,255,0.20), 12, 0.22, 0, 3);" +
               "-fx-cursor:hand;";
    }

    private static VBox buildMainContent(StringProperty searchQuery) {
        VBox main = new VBox(16);
        main.setPadding(new Insets(4, 6, 4, 6));
        main.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(main, Priority.ALWAYS);

        main.getChildren().addAll(buildHeader(searchQuery), buildSummaryRow(), buildUpcomingPanel(searchQuery));
        return main;
    }

    private static HBox buildHeader(StringProperty searchQuery) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 2, 2, 2));

        VBox titles = new VBox(2);
        Label title = new Label("Overview");
        title.setStyle("-fx-font-size:25px; -fx-font-weight:bold; -fx-text-fill:" + Theme.CREAM + ";");
        Label sub = new Label("Welcome back, Administrator.");
        sub.setStyle(Theme.subtitleStyle());
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane searchWrap = new StackPane();
        searchWrap.setMaxWidth(300);
        TextField search = new TextField();
        search.setPromptText("Search bookings...");
        search.setPrefWidth(300);
        search.setStyle(Theme.inputStyle() + "-fx-background-radius:18; -fx-border-radius:18; -fx-padding:10 14 10 34;");
        search.textProperty().addListener((obs, oldValue, newValue) ->
            searchQuery.set(newValue == null ? "" : newValue.trim())
        );
        Label searchIcon = new Label("⌕");
        searchIcon.setStyle("-fx-text-fill:" + Theme.CREAM_DIM + "; -fx-font-size:15px;");
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 12));
        searchWrap.getChildren().addAll(search, searchIcon);

        VBox profile = new VBox(2);
        profile.setAlignment(Pos.CENTER_RIGHT);
        Label name = new Label("Admin Profile");
        name.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + Theme.CREAM + ";");
        String adminEmail = controllers.LoginController.loggedAdmin != null 
            ? controllers.LoginController.loggedAdmin.getEmail() 
            : "admin@hall.com";
        Label mail = new Label(adminEmail);
        mail.setStyle("-fx-font-size:11px; -fx-text-fill:" + Theme.CREAM_DIM + ";");

        StackPane avatar = new StackPane();
        avatar.setPrefSize(34, 34);
        avatar.setMaxSize(34, 34);
        avatar.setStyle("-fx-background-color: linear-gradient(to bottom right, " + Theme.GOLD_LIGHT + ", " + Theme.GOLD + "); -fx-background-radius:999; -fx-border-radius:999; -fx-border-color: rgba(255,255,255,0.7); -fx-border-width:1.2;");
        Label avatarText = new Label("A");
        avatarText.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:13px;");
        avatar.getChildren().add(avatarText);

        HBox profileRow = new HBox(10, new VBox(2, name, mail), avatar);
        profileRow.setAlignment(Pos.CENTER_RIGHT);
        profile.getChildren().add(profileRow);

        header.getChildren().addAll(titles, spacer, searchWrap, profile);
        return header;
    }

    private static HBox buildSummaryRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
            statCard("🏛", totalHalls(), "TOTAL HALLS", hallBadge()),
            statCard("🛎", totalServices(), "ACTIVE SERVICES", serviceBadge()),
            statCard("📋", confirmedBookings(), "CONFIRMED BOOKINGS", bookingBadge()),
            statCard("👥", uniqueCustomers(), "UNIQUE CUSTOMERS", customerBadge())
        );
        return row;
    }

    private static VBox statCard(String icon, long count, String label, String badgeText) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(186, 140);
        card.setMinSize(186, 140);
        card.setPadding(new Insets(16, 16, 14, 16));
        card.setStyle(Theme.cardStyle() + "-fx-background-radius:14; -fx-border-radius:14; -fx-border-color: rgba(233,217,197,0.8);");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:18px; -fx-text-fill:" + Theme.GOLD + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label(badgeText);
        badge.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + Theme.SUCCESS + "; -fx-background-color: rgba(109,191,130,0.12); -fx-background-radius:999; -fx-padding:4 10 4 10;");
        header.getChildren().addAll(iconLbl, spacer, badge);

        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" + Theme.CREAM + ";");

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + Theme.CREAM_DIM + ";");

        card.getChildren().addAll(header, countLbl, nameLbl);

        card.setOnMouseEntered(e -> {
            card.setStyle(Theme.cardStyle() + "-fx-background-color: rgba(255,253,248,0.995); -fx-border-color: rgba(184,137,59,0.62); -fx-background-radius:14; -fx-border-radius:14;");
            card.setTranslateY(-3);
            card.setScaleX(1.015);
            card.setScaleY(1.015);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(Theme.cardStyle() + "-fx-background-radius:14; -fx-border-radius:14; -fx-border-color: rgba(233,217,197,0.8);");
            card.setTranslateY(0);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        ScaleTransition pulse = new ScaleTransition(Duration.millis(3200), card);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.01);
        pulse.setToY(1.01);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        return card;
    }

    private static VBox buildUpcomingPanel(StringProperty searchQuery) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(0));
        panel.setStyle(Theme.cardStyle() + "-fx-background-radius:14; -fx-border-radius:14;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 0, 22));

        VBox titles = new VBox(2);
        Label title = new Label("Upcoming Bookings");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + Theme.CREAM + ";");
        Label sub = new Label("Monitor your schedule for the next 30 days");
        sub.setStyle(Theme.subtitleStyle());
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All Bookings");
        viewAll.setStyle(Theme.goldButtonStyle() + "-fx-background-radius:8; -fx-padding:8 16; -fx-font-size:13px;");
        viewAll.setOnAction(e -> SceneManager.switchTo("bookings"));

        header.getChildren().addAll(titles, spacer, viewAll);

        TableView<Booking> table = new TableView<>();
        table.setStyle(Theme.tableStyle() + "-fx-background-radius:0 0 14 14; -fx-border-radius:0 0 14 14;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(305);
        table.setMinHeight(0);
        table.setMaxHeight(Double.MAX_VALUE);

        TableColumn<Booking, String> detailCol = new TableColumn<>("EVENT DETAIL");
        detailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(buildEventDetail(data.getValue())));

        TableColumn<Booking, String> hallCol = new TableColumn<>("HALL");
        hallCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getHall() != null ? data.getValue().getHall().getName() : ""));

        TableColumn<Booking, String> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(buildDateDetail(data.getValue())));

        TableColumn<Booking, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        TableColumn<Booking, String> revenueCol = new TableColumn<>("REVENUE");
        revenueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("$%,.2f", data.getValue().getTotalPrice())));

        table.getColumns().addAll(detailCol, hallCol, dateCol, statusCol, revenueCol);

        table.setRowFactory(tv -> new TableRow<>() {
            {
                selectedProperty().addListener((obs, oldValue, newValue) -> updateItem(getItem(), isEmpty()));
            }

            @Override
            protected void updateItem(Booking item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle(
                        "-fx-background-color:" + Theme.GOLD + ";" +
                        "-fx-text-fill:#FFFFFF;" +
                        "-fx-font-family:'Georgia';"
                    );
                } else if ("CONFIRMED".equalsIgnoreCase(item.getStatus())) {
                    setStyle("-fx-background-color: rgba(109,191,130,0.05);");
                } else if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                    setStyle("-fx-background-color: rgba(240,203,94,0.07);");
                } else if ("CANCELED".equalsIgnoreCase(item.getStatus())) {
                    setStyle("-fx-background-color: rgba(224,85,85,0.06);");
                } else {
                    setStyle("");
                }
            }
        });

        // Create observable list with all upcoming bookings
        javafx.collections.ObservableList<Booking> upcomingList = FXCollections.observableArrayList(upcomingBookings());
        FilteredList<Booking> filteredUpcoming = new FilteredList<>(upcomingList, booking -> true);
        SortedList<Booking> sortedUpcoming = new SortedList<>(filteredUpcoming);
        sortedUpcoming.comparatorProperty().bind(table.comparatorProperty());

        Runnable applySearchFilter = () -> {
            String query = searchQuery.get() == null ? "" : searchQuery.get().trim().toLowerCase(Locale.ENGLISH);
            filteredUpcoming.setPredicate(booking -> matchesBookingQuery(booking, query));
        };
        applySearchFilter.run();
        searchQuery.addListener((obs, oldValue, newValue) -> applySearchFilter.run());

        table.setItems(sortedUpcoming);
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().addAll(header, new Separator(), table);
        return panel;
    }

    private static String buildEventDetail(Booking booking) {
        String customer = booking.getCustomer() != null ? booking.getCustomer().getName() : "Unknown";
        return customer + " Wedding";
    }

    private static String buildDateDetail(Booking booking) {
        if (booking.getEventDate() == null) return "";
        return booking.getEventDate().format(DATE_FMT);
    }

    private static long totalHalls() {
        return HallController.getAll().size();
    }

    private static long totalServices() {
        return ServiceController.getAll().size();
    }

    private static Admin getLoggedAdmin() {
        return controllers.LoginController.loggedAdmin;
    }

    private static long confirmedBookings() {
        Admin admin = getLoggedAdmin();
        return BookingController.getAll().stream()
            .filter(b -> admin != null && admin.equals(b.getCreatedBy()))
            .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
            .count();
    }

    private static long uniqueCustomers() {
        Admin admin = getLoggedAdmin();
        return BookingController.getAll().stream()
            .filter(b -> admin != null && admin.equals(b.getCreatedBy()))
            .map(Booking::getCustomer)
            .distinct()
            .count();
    }

    private static List<Booking> upcomingBookings() {
        Admin admin = getLoggedAdmin();
        return BookingController.getAll().stream()
            .filter(b -> admin != null && admin.equals(b.getCreatedBy()))
            .filter(b -> b.getEventDate() != null)
            .filter(b -> !b.getEventDate().isBefore(LocalDate.now()))
            .filter(b -> !"CANCELED".equalsIgnoreCase(b.getStatus()))
            .sorted(Comparator.comparing(Booking::getEventDate))
            .collect(Collectors.toList());
    }

    private static boolean matchesBookingQuery(Booking booking, String query) {
        if (query == null || query.isBlank()) return true;

        Admin admin = getLoggedAdmin();
        if (admin == null || !admin.equals(booking.getCreatedBy())) return false;

        String customerName = booking.getCustomer() != null ? booking.getCustomer().getName() : "";
        String hallName = booking.getHall() != null ? booking.getHall().getName() : "";
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        String eventDate = booking.getEventDate() != null ? booking.getEventDate().format(DATE_FMT) : "";
        String total = String.format("%.2f", booking.getTotalPrice());

        String haystack = (customerName + " " + hallName + " " + status + " " + eventDate + " " + total).toLowerCase(Locale.ENGLISH);
        return haystack.contains(query);
    }

    private static String hallBadge() {
        return totalHalls() + " halls";
    }

    private static String serviceBadge() {
        return totalServices() + " active";
    }

    private static String bookingBadge() {
        return upcomingBookings().size() + " upcoming";
    }

    private static String customerBadge() {
        return uniqueCustomers() + " customers";
    }
}
