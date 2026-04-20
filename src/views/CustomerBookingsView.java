package views;

import controllers.BookingController;
import controllers.LoginController;
import hall_wedding.Booking;
import hall_wedding.SceneManager;
import hall_wedding.Theme;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

public class CustomerBookingsView {

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(980, 600);
        root.setTop(topBar());

        TableView<Booking> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Booking, Long> cId = col("ID", "id", 70);
        TableColumn<Booking, LocalDate> cDate = col("Date", "eventDate", 130);
        TableColumn<Booking, Integer> cHours = col("Hours", "durationHours", 90);
        TableColumn<Booking, String> cStatus = col("Status", "status", 110);
        TableColumn<Booking, Double> cTotal = col("Total", "totalPrice", 120);

        TableColumn<Booking, String> cHall = new TableColumn<>("Hall");
        cHall.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getHall() != null ? d.getValue().getHall().getName() : ""
        ));
        cHall.setPrefWidth(180);

        TableColumn<Booking, String> cPaid = new TableColumn<>("Payment");
        cPaid.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().isPaid() ? "Paid" : "Pending"
        ));
        cPaid.setPrefWidth(110);

        table.getColumns().addAll(cId, cHall, cDate, cHours, cStatus, cTotal, cPaid);

        Label info = new Label();
        info.setStyle(Theme.subtitleStyle());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(Theme.goldButtonStyle());
        refreshBtn.setOnAction(e -> reload(table, info));

        VBox center = new VBox(14, info, refreshBtn, table);
        center.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(center);

        reload(table, info);
        return new Scene(root, 980, 600);
    }

    private static void reload(TableView<Booking> table, Label info) {
        if (LoginController.loggedCustomer == null || LoginController.loggedCustomer.getId() == null) {
            info.setText("No logged-in customer.");
            table.setItems(FXCollections.observableArrayList());
            return;
        }

        var rows = BookingController.getByCustomer(LoginController.loggedCustomer.getId());
        info.setText("My bookings: " + rows.size());
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private static HBox topBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 24, 16, 24));
        bar.setStyle(Theme.frostedPanelStyle());

        Label title = new Label("My Bookings");
        title.setStyle(Theme.titleStyle(22));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.navButtonStyle());
        logoutBtn.setOnAction(e -> {
            LoginController.logout();
            SceneManager.switchTo("login");
        });

        bar.getChildren().addAll(title, logoutBtn);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private static <S, T> TableColumn<S, T> col(String h, String p, double w) {
        TableColumn<S, T> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(p));
        c.setPrefWidth(w);
        return c;
    }
}
