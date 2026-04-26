package views;

import controllers.ServiceController;
import hall_wedding.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class ServiceView {

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    private static final ObservableList<String> SERVICE_TYPES = FXCollections.observableArrayList(
        "Food", "Decor", "Photography", "Video", "Music", "Lighting",
        "Flower", "Cake", "Cars", "Security", "Coordination", "Other"
    );

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(920, 580);
        root.setTop(HallView.topBar("🛎️  Service Management", "admin"));

        TextField nameF  = field("Service Name");
        TextField priceF = field("Price");
        ComboBox<String> typeF = combo("Type", SERVICE_TYPES);
        Label     msgLbl = new Label("");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setPrefWidth(320);

        TableView<Service> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
            col("ID",    "id",    60),
            col("Name",  "name",  160),
            col("Type",  "type",  120),
            col("Price", "price", 100)
        );
        reload(table);

        table.getSelectionModel().selectedItemProperty().addListener((o, old, s) -> {
            if (s != null) {
                nameF.setText(s.getName());
                priceF.setText(String.valueOf(s.getPrice()));
                typeF.setValue(s.getType());
            }
        });

        Button addBtn = btn("Add Service",    Theme.goldButtonStyle() + "-fx-font-size:13px; -fx-padding:8 14;");
        Button updBtn = btn("Update Service", Theme.roseButtonStyle());
        Button delBtn = btn("Delete Service", Theme.dangerButtonStyle());

        addBtn.setOnAction(e -> {
            try {
                String name = nameF.getText().trim();
                String priceTxt = priceF.getText().trim();
                String type = typeF.getValue();

                if (name.isEmpty() || priceTxt.isEmpty() || type == null) {
                    err(msgLbl, "⚠️ All fields are required");
                    return;
                }
                if (name.length() < 3) {
                    err(msgLbl, "⚠️ Service name must be at least 3 characters");
                    return;
                }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    err(msgLbl, "⚠️ Service name must contain letters only");
                    return;
                }

                double price = Double.parseDouble(priceTxt);
                if (price <= 0) {
                    err(msgLbl, "⚠️ Price must be greater than 0");
                    return;
                }

                ServiceController.add(name,         // ← Controller
                    price, type);
                ok(msgLbl, "✅ Service added!"); reload(table);
                nameF.clear();
                priceF.clear();
                typeF.setValue(null);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        updBtn.setOnAction(e -> {
            Service sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a service"); return; }
            try {
                String name = nameF.getText().trim();
                String priceTxt = priceF.getText().trim();
                String type = typeF.getValue();

                if (name.isEmpty() || priceTxt.isEmpty() || type == null) {
                    err(msgLbl, "⚠️ All fields are required");
                    return;
                }
                if (name.length() < 3) {
                    err(msgLbl, "⚠️ Service name must be at least 3 characters");
                    return;
                }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    err(msgLbl, "⚠️ Service name must contain letters only");
                    return;
                }

                double price = Double.parseDouble(priceTxt);
                if (price <= 0) {
                    err(msgLbl, "⚠️ Price must be greater than 0");
                    return;
                }

                ServiceController.update(sel.getId(), name, // ← Controller
                    price, type);
                ok(msgLbl, "✅ Updated!"); reload(table);
            } catch (Exception ex) {
                err(msgLbl, "❌ " + ex.getMessage());
            }
        });

        delBtn.setOnAction(e -> {
            Service sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a service"); return; }
            ServiceController.delete(sel.getId());                 // ← Controller
            err(msgLbl, "🗑️ Deleted!"); reload(table);
            nameF.clear();
            priceF.clear();
            typeF.setValue(null);
        });

        VBox form = new VBox(12);
        form.setPadding(new Insets(24)); form.setPrefWidth(360);
        form.setStyle(Theme.cardStyle());
        Label ft = new Label("Add / Edit Service"); ft.setStyle(Theme.titleStyle(15));
        VBox actions = new VBox(8, addBtn, updBtn, delBtn);
        form.getChildren().addAll(ft,
            labeled("Service Name", nameF),
            labeled("Price", priceF),
            labeled("Service Type", typeF),
            actions, msgLbl);

        HBox center = new HBox(20); center.setPadding(new Insets(20));
        HBox.setHgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(form, table);
        root.setCenter(center);
        return new Scene(root, 920, 580);
    }

    private static void reload(TableView<Service> t) {
        t.setItems(FXCollections.observableArrayList(ServiceController.getAll()));
    }

    @SuppressWarnings("unchecked")
    private static <S,T> TableColumn<S,T> col(String h, String p, double w) {
        TableColumn<S,T> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); return c;
    }

    private static TextField field(String p) {
        TextField f = new TextField(); f.setPromptText(p); f.setStyle(Theme.inputStyle()); return f;
    }

    private static ComboBox<String> combo(String prompt, ObservableList<String> items) {
        ComboBox<String> comboBox = new ComboBox<>(items);
        comboBox.setPromptText(prompt);
        comboBox.setStyle(Theme.inputStyle());
        comboBox.setMaxWidth(Double.MAX_VALUE);
        return comboBox;
    }

    private static VBox labeled(String title, Node field) {
        Label label = new Label(title);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        return new VBox(4, label, field);
    }

    private static Button btn(String t, String s) {
        Button b = new Button(t);
        b.setStyle(s);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }
    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }
}