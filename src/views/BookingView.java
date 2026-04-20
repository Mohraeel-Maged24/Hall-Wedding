package views;

import controllers.*;
import hall_wedding.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.LocalDate;

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
        custCombo.setConverter(converter(c -> c == null ? "" : c.getName()));

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

        Label msgLbl   = new Label(""); msgLbl.setStyle("-fx-font-size:12px;");
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
            d.getValue().getCustomer() != null ? d.getValue().getCustomer().getName() : ""));
        cCust.setPrefWidth(130);

        TableColumn<Booking,String> cHall = new TableColumn<>("Hall");
        cHall.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getHall() != null ? d.getValue().getHall().getName() : ""));
        cHall.setPrefWidth(110);

        table.getColumns().addAll(cId, cCust, cHall, cDate, cStat, cTot);
        reload(table);

        // ── Buttons ───────────────────────────────────────
        Button createBtn = new Button("📋  Create Booking"); createBtn.setStyle(Theme.goldButtonStyle());
        Button payBtn    = new Button("💳  Pay");            payBtn.setStyle(Theme.roseButtonStyle());
        Button cancelBtn = new Button("❌  Cancel");         cancelBtn.setStyle(Theme.dangerButtonStyle());

        createBtn.setOnAction(e -> {
            Customer c = custCombo.getValue();
            Hall     h = hallCombo.getValue();
            LocalDate d = datePicker.getValue();
            if (c == null || h == null || d == null || durationF.getText().isEmpty()) {
                err(msgLbl, "⚠️ Fill all fields"); return;
            }
            try {
                double total = BookingController.create(    // ← Controller
                    c, h, d, Integer.parseInt(durationF.getText()));
                priceLbl.setText("Total: " + total + " EGP");
                ok(msgLbl, "✅ Booking created!"); reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        payBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select booking"); return; }
            boolean done = BookingController.pay(sel.getId()); // ← Controller
            if (done) ok(msgLbl, "💳 Paid!");
            else      err(msgLbl, "❌ Must be CONFIRMED first");
            reload(table);
        });

        cancelBtn.setOnAction(e -> {
            Booking sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select booking"); return; }
            BookingController.cancel(sel.getId());              // ← Controller
            err(msgLbl, "🗑️ Booking cancelled"); reload(table);
        });

        // ── Form Card ────────────────────────────────────
        VBox form = new VBox(12); form.setPadding(new Insets(20)); form.setPrefWidth(300);
        form.setStyle(Theme.cardStyle());
        Label ft = new Label("New Booking"); ft.setStyle(Theme.titleStyle(15));
        form.getChildren().addAll(ft, custCombo, hallCombo, datePicker, durationF,
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

    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }
}