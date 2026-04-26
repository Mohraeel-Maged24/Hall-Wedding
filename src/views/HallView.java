package views;

import controllers.HallController;
import hall_wedding.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class HallView {

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    private static final ObservableList<String> GOVERNORATES = FXCollections.observableArrayList(
        "Cairo", "Giza", "Alexandria", "Dakahlia", "Red Sea", "Beheira", "Fayoum",
        "Gharbia", "Ismailia", "Menofia", "Minya", "Qaliubiya", "New Valley",
        "Suez", "Aswan", "Assiut", "Beni Suef", "Port Said", "Damietta",
        "Sharkia", "South Sinai", "Kafr El Sheikh", "Matrouh", "Luxor", "Qena",
        "North Sinai", "Sohag"
    );

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(920, 580);
        root.setTop(topBar("🏛️  Hall Management", "admin"));

        // ── Fields ───────────────────────────────────────
        TextField nameF     = field("Hall Name");
        ComboBox<String> locationF = combo("Location", GOVERNORATES);
        TextField capacityF = field("Capacity");
        TextField priceF    = field("Price Per Hour");
        Label     msgLbl    = new Label("");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setPrefWidth(320);

        // ── Table ─────────────────────────────────────────
        TableView<Hall> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
            col("ID",       "id",           60),
            col("Name",     "name",        150),
            col("Location", "location",    130),
            col("Capacity", "capacity",     90),
            col("Price/hr", "pricePerHour",100)
        );
        reload(table);

        // Click row → fill form
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                nameF.setText(sel.getName());
                locationF.setValue(sel.getLocation());
                capacityF.setText(String.valueOf(sel.getCapacity()));
                priceF.setText(String.valueOf(sel.getPricePerHour()));
            }
        });

        // ── Buttons ───────────────────────────────────────
        Button addBtn = new Button("Add Hall");
        addBtn.setStyle(Theme.goldButtonStyle() + "-fx-font-size:13px; -fx-padding:8 16;");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            try {
                String name = nameF.getText().trim();
                String location = locationF.getValue();
                String capacityTxt = capacityF.getText().trim();
                String priceTxt = priceF.getText().trim();

                if (name.isEmpty() || location == null || capacityTxt.isEmpty() || priceTxt.isEmpty()) {
                    err(msgLbl, "⚠️ All fields are required");
                    return;
                }
                if (name.length() < 3) {
                    err(msgLbl, "⚠️ Hall name must be at least 3 characters");
                    return;
                }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    err(msgLbl, "⚠️ Hall name must contain letters only");
                    return;
                }

                int capacity = Integer.parseInt(capacityTxt);
                double price = Double.parseDouble(priceTxt);
                if (capacity < 10 || capacity > 10000) {
                    err(msgLbl, "⚠️ Capacity must be between 10 and 10000");
                    return;
                }
                if (price < 5000) {
                    err(msgLbl, "⚠️ Price must be at least 5000");
                    return;
                }

                HallController.add(                         // ← Controller فقط
                    name, location,
                    capacity,
                    price);
                ok(msgLbl, "✅ Hall added!");
                clear(nameF, capacityF, priceF);
                locationF.setValue(null);
                reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        Button updateBtn = new Button("Update Hall");
        updateBtn.setStyle(Theme.roseButtonStyle());
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setOnAction(e -> {
            Hall sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a hall"); return; }
            try {
                String name = nameF.getText().trim();
                String location = locationF.getValue();
                String capacityTxt = capacityF.getText().trim();
                String priceTxt = priceF.getText().trim();

                if (name.isEmpty() || location == null || capacityTxt.isEmpty() || priceTxt.isEmpty()) {
                    err(msgLbl, "⚠️ All fields are required");
                    return;
                }
                if (name.length() < 3) {
                    err(msgLbl, "⚠️ Hall name must be at least 3 characters");
                    return;
                }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    err(msgLbl, "⚠️ Hall name must contain letters only");
                    return;
                }

                int capacity = Integer.parseInt(capacityTxt);
                double price = Double.parseDouble(priceTxt);
                if (capacity < 10 || capacity > 10000) {
                    err(msgLbl, "⚠️ Capacity must be between 10 and 10000");
                    return;
                }
                if (price < 5000) {
                    err(msgLbl, "⚠️ Price must be at least 5000");
                    return;
                }

                HallController.update(                      // ← Controller فقط
                    sel.getId(), name, location,
                    capacity,
                    price);
                ok(msgLbl, "✅ Updated!"); reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        Button deleteBtn = new Button("Delete Hall");
        deleteBtn.setStyle(Theme.dangerButtonStyle());
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> {
            Hall sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a hall"); return; }
            try {
                HallController.delete(sel.getId());             // ← Controller فقط
                err(msgLbl, "🗑️ Deleted!");
                reload(table);
            } catch (Exception ex) {
                err(msgLbl, "⚠️ " + ex.getMessage());
            }
        });

        // ── Form Card ────────────────────────────────────
        VBox form = new VBox(12);
        form.setPadding(new Insets(24)); form.setPrefWidth(360);
        form.setStyle(Theme.cardStyle());
        Label formTitle = new Label("Add / Edit Hall"); formTitle.setStyle(Theme.titleStyle(15));
        VBox btnRow = new VBox(8, addBtn, updateBtn, deleteBtn);
        form.getChildren().addAll(formTitle,
            labeled("Hall Name", nameF),
            labeled("Governorate", locationF),
            labeled("Capacity", capacityF),
            labeled("Price Per Hour", priceF),
            btnRow, msgLbl);

        HBox center = new HBox(20);
        center.setPadding(new Insets(20));
        HBox.setHgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(form, table);
        root.setCenter(center);
        return new Scene(root, 920, 580);
    }

    private static void reload(TableView<Hall> t) {
        t.setItems(FXCollections.observableArrayList(HallController.getAll())); // ← Controller
    }

    @SuppressWarnings("unchecked")
    private static <S, T> TableColumn<S, T> col(String h, String p, double w) {
        TableColumn<S, T> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(p));
        c.setPrefWidth(w);
        return c;
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

    private static void clear(TextField... fs) { for (TextField f : fs) f.clear(); }
    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }

    static HBox topBar(String title, String backPage) {
        HBox bar = new HBox(14); bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 24, 16, 24));
        bar.setStyle(Theme.frostedPanelStyle());
        Button back = new Button("Back"); back.setStyle(Theme.navButtonStyle() + "-fx-font-size:14px;");
        back.setOnAction(e -> SceneManager.switchTo(backPage));
        Label lbl = new Label(title); lbl.setStyle(Theme.titleStyle(20));
        bar.getChildren().addAll(back, lbl); return bar;
    }
}