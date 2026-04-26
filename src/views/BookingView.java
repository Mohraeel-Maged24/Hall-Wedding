package views;

import controllers.*;
import hall_wedding.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BookingView {

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(980, 600);
        root.setTop(HallView.topBar("📋  Booking Management", "admin"));

        // ── Combos ───────────────────────────────────────
        ComboBox<Customer> custCombo = new ComboBox<>(
            FXCollections.observableArrayList(CustomerController.getAll())); // ← Controller
        custCombo.setPromptText("Select Customer");
        custCombo.setStyle(Theme.inputStyle()); custCombo.setMaxWidth(260);
        custCombo.setConverter(converter(c -> {
            if (c == null) return "";
            return c.getName() + " - " + c.getSsn();
        }));

        ComboBox<Hall> hallCombo = new ComboBox<>(
            FXCollections.observableArrayList(HallController.getAll()));     // ← Controller
        hallCombo.setPromptText("Select Hall");
        hallCombo.setStyle(Theme.inputStyle()); hallCombo.setMaxWidth(260);
        hallCombo.setConverter(converter(h -> h == null ? "" : h.getName()));

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Event Date");
        datePicker.setStyle(Theme.inputStyle()); datePicker.setMaxWidth(260);

        TextField durationF = new TextField();
        durationF.setPromptText("Duration (hours)");
        durationF.setStyle(Theme.inputStyle());

        ComboBox<String> paymentCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Cash", "Vodafone Cash", "InstaPay", "Bank Transfer"
        ));
        paymentCombo.setPromptText("Payment Method");
        paymentCombo.setStyle(Theme.inputStyle());
        paymentCombo.setMaxWidth(260);

        ComboBox<Service> serviceCombo = new ComboBox<>(
            FXCollections.observableArrayList(ServiceController.getAll()));
        serviceCombo.setPromptText("Select Service");
        serviceCombo.setStyle(Theme.inputStyle());
        serviceCombo.setMaxWidth(260);
        serviceCombo.setConverter(converter(s -> s == null
            ? ""
            : s.getName() + " - " + String.format("%.2f", s.getPrice()) + " EGP (" + s.getType() + ")"));

        ListView<Service> selectedServicesView = new ListView<>(FXCollections.observableArrayList());
        selectedServicesView.setPrefHeight(140);
        selectedServicesView.setStyle(Theme.inputStyle());
        selectedServicesView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                    ? null
                    : item.getName() + " - " + String.format("%.2f", item.getPrice()) + " EGP (" + item.getType() + ")");
            }
        });

        Button addServiceBtn = new Button("Add Service");
        addServiceBtn.setStyle(Theme.roseButtonStyle());
        Button removeServiceBtn = new Button("Remove Service");
        removeServiceBtn.setStyle(Theme.dangerButtonStyle());

        Label selectedServicesLbl = new Label("Selected services: none");
        selectedServicesLbl.setWrapText(true);
        selectedServicesLbl.setMaxWidth(320);
        selectedServicesLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + Theme.CREAM_DIM + ";");

        Label msgLbl   = new Label(""); msgLbl.setStyle("-fx-font-size:12px;");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setPrefWidth(340);
        Label priceLbl = new Label(""); priceLbl.setStyle("-fx-font-size:13px; -fx-text-fill:" + Theme.GOLD + ";");

        // ── Table ─────────────────────────────────────────
        TableView<Booking> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Booking,Long>      cId   = col("ID",      "id",         50);
        TableColumn<Booking,LocalDate> cDate = col("Date",    "eventDate",  110);
        TableColumn<Booking,String>    cStat = col("Status",  "status",      90);
        TableColumn<Booking,Double>    cTot  = col("Total",   "totalPrice",  90);

        TableColumn<Booking,String> cCust = new TableColumn<>("Customer");
        cCust.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getCustomer() != null
                ? d.getValue().getCustomer().getName() + " - " + d.getValue().getCustomer().getSsn()
                : ""));
        cCust.setPrefWidth(130);

        TableColumn<Booking,String> cHall = new TableColumn<>("Hall");
        cHall.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getHall() != null ? d.getValue().getHall().getName() : ""));
        cHall.setPrefWidth(110);

        table.getColumns().addAll(cId, cCust, cHall, cDate, cStat, cTot);
        reload(table);

        // ── Buttons ───────────────────────────────────────
        Button createBtn = new Button("Create Booking"); createBtn.setStyle(Theme.goldButtonStyle());
        Button payBtn    = new Button("Pay Booking");    payBtn.setStyle(Theme.roseButtonStyle());
        Button cancelBtn = new Button("Cancel Booking"); cancelBtn.setStyle(Theme.dangerButtonStyle());

        addServiceBtn.setOnAction(e -> {
            Service selected = serviceCombo.getValue();
            if (selected == null) {
                err(msgLbl, "⚠️ Select service first");
                return;
            }
            if (!selectedServicesView.getItems().contains(selected)) {
                selectedServicesView.getItems().add(selected);
            }
            refreshSelectedServicesLabel(selectedServicesView.getItems(), selectedServicesLbl);
            serviceCombo.setValue(null);
        });

        removeServiceBtn.setOnAction(e -> {
            Service selected = selectedServicesView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                err(msgLbl, "⚠️ Select a service from selected list to remove");
                return;
            }
            selectedServicesView.getItems().remove(selected);
            refreshSelectedServicesLabel(selectedServicesView.getItems(), selectedServicesLbl);
        });

        createBtn.setOnAction(e -> {
            Customer c = custCombo.getValue();
            Hall     h = hallCombo.getValue();
            LocalDate d = datePicker.getValue();
            String durationTxt = durationF.getText().trim();
            List<Service> selectedServices = selectedServicesView.getItems();
            if (c == null || h == null || d == null || durationTxt.isEmpty()) {
                err(msgLbl, "⚠️ Fill all fields"); return;
            }
            if (selectedServices == null || selectedServices.isEmpty()) {
                err(msgLbl, "⚠️ Select at least one service");
                return;
            }
            if (d.isBefore(LocalDate.now())) {
                err(msgLbl, "⚠️ Event date cannot be in the past");
                return;
            }
            try {
                int duration = Integer.parseInt(durationTxt);
                if (duration < 1 || duration > 24) {
                    err(msgLbl, "⚠️ Duration must be between 1 and 24 hours");
                    return;
                }

                double total = BookingController.create(    // ← Controller
                    c, h, d, duration, selectedServices);
                priceLbl.setText("Total: " + total + " EGP");
                ok(msgLbl, "✅ Booking created with status PENDING. Choose payment method then click Pay."); reload(table);
                durationF.clear();
                selectedServicesView.getItems().clear();
                selectedServicesLbl.setText("Selected services: none");
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        payBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select booking"); return; }
            String paymentMethod = paymentCombo.getValue();
            if (paymentMethod == null) {
                err(msgLbl, "⚠️ Select payment method first");
                return;
            }
            boolean done = BookingController.pay(sel.getId(), paymentMethod); // ← Controller
            if (done) ok(msgLbl, "💳 Paid!");
            else      err(msgLbl, "❌ Cannot pay this booking");
            reload(table);
        });

        cancelBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select booking"); return; }
            BookingController.cancel(sel.getId());              // ← Controller
            err(msgLbl, "🗑️ Booking cancelled"); reload(table);
        });

        // ── Form Card ────────────────────────────────────
        VBox form = new VBox(12); form.setPadding(new Insets(20)); form.setPrefWidth(380);
        form.setStyle(Theme.cardStyle());
        Label ft = new Label("New Booking"); ft.setStyle(Theme.titleStyle(15));
        form.getChildren().addAll(ft,
            labeled("Customer", custCombo),
            labeled("Hall", hallCombo),
            labeled("Event Date", datePicker),
            labeled("Duration (hours)", durationF),
            labeled("Service", serviceCombo),
            new HBox(8, addServiceBtn, removeServiceBtn),
            labeled("Selected Services", selectedServicesView),
            selectedServicesLbl,
            labeled("Payment Method", paymentCombo),
            createBtn, payBtn, cancelBtn, priceLbl, msgLbl);

        HBox center = new HBox(20); center.setPadding(new Insets(20));
        HBox.setHgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(form, table);
        root.setCenter(center);
        return new Scene(root, 980, 600);
    }

    private static void reload(TableView<Booking> t) {
        t.setItems(FXCollections.observableArrayList(BookingController.getAll()));
    }

    @SuppressWarnings("unchecked")
    private static <S,T> TableColumn<S,T> col(String h, String p, double w) {
        TableColumn<S,T> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); return c;
    }

    private static <T> javafx.util.StringConverter<T> converter(java.util.function.Function<T,String> fn) {
        return new javafx.util.StringConverter<>() {
            public String toString(T obj) { return fn.apply(obj); }
            public T fromString(String s) { return null; }
        };
    }

    private static VBox labeled(String title, Node field) {
        Label label = new Label(title);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        return new VBox(4, label, field);
    }

    private static void refreshSelectedServicesLabel(List<Service> services, Label label) {
        if (services == null || services.isEmpty()) {
            label.setText("Selected services: none");
            return;
        }
        String text = services.stream()
            .map(service -> service.getName() + " (" + String.format("%.2f", service.getPrice()) + " EGP)")
            .collect(Collectors.joining(" , "));
        label.setText("Selected services: " + text);
    }

    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }
}