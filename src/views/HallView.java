package views;

import controllers.HallController;
import hall_wedding.*;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class HallView {

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(920, 580);
        root.setTop(topBar("🏛️  Hall Management", "admin"));

        // ── Fields ───────────────────────────────────────
        TextField nameF     = field("Hall Name");
        TextField locationF = field("Location");
        TextField capacityF = field("Capacity");
        TextField priceF    = field("Price Per Hour");
        Label     msgLbl    = new Label("");

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
                locationF.setText(sel.getLocation());
                capacityF.setText(String.valueOf(sel.getCapacity()));
                priceF.setText(String.valueOf(sel.getPricePerHour()));
            }
        });

        // ── Buttons ───────────────────────────────────────
        Button addBtn = new Button("➕  Add");
        addBtn.setStyle(Theme.goldButtonStyle() + "-fx-font-size:13px; -fx-padding:8 16;");
        addBtn.setOnAction(e -> {
            try {
                HallController.add(                         // ← Controller فقط
                    nameF.getText(), locationF.getText(),
                    Integer.parseInt(capacityF.getText()),
                    Double.parseDouble(priceF.getText()));
                ok(msgLbl, "✅ Hall added!");
                clear(nameF, locationF, capacityF, priceF);
                reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        Button updateBtn = new Button("✏️  Update");
        updateBtn.setStyle(Theme.roseButtonStyle());
        updateBtn.setOnAction(e -> {
            Hall sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a hall"); return; }
            try {
                HallController.update(                      // ← Controller فقط
                    sel.getId(), nameF.getText(), locationF.getText(),
                    Integer.parseInt(capacityF.getText()),
                    Double.parseDouble(priceF.getText()));
                ok(msgLbl, "✅ Updated!"); reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        Button deleteBtn = new Button("🗑️  Delete");
        deleteBtn.setStyle(Theme.dangerButtonStyle());
        deleteBtn.setOnAction(e -> {
            Hall sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a hall"); return; }
            HallController.delete(sel.getId());             // ← Controller فقط
            err(msgLbl, "🗑️ Deleted!"); reload(table);
        });

        // ── Form Card ────────────────────────────────────
        VBox form = new VBox(12);
        form.setPadding(new Insets(24)); form.setPrefWidth(280);
        form.setStyle(Theme.cardStyle());
        Label formTitle = new Label("Add / Edit Hall"); formTitle.setStyle(Theme.titleStyle(15));
        HBox btnRow = new HBox(8, addBtn, updateBtn, deleteBtn);
        form.getChildren().addAll(formTitle, nameF, locationF, capacityF, priceF, btnRow, msgLbl);

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

    private static void clear(TextField... fs) { for (TextField f : fs) f.clear(); }
    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }

    static HBox topBar(String title, String backPage) {
        HBox bar = new HBox(14); bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 24, 16, 24));
        bar.setStyle(Theme.frostedPanelStyle());
        Button back = new Button("⬅"); back.setStyle(Theme.navButtonStyle() + "-fx-font-size:18px;");
        back.setOnAction(e -> SceneManager.switchTo(backPage));
        Label lbl = new Label(title); lbl.setStyle(Theme.titleStyle(20));
        bar.getChildren().addAll(back, lbl); return bar;
    }
}