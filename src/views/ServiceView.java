package views;

import controllers.ServiceController;
import hall_wedding.*;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class ServiceView {

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(920, 580);
        root.setTop(HallView.topBar("🛎️  Service Management", "admin"));

        TextField nameF  = field("Service Name");
        TextField priceF = field("Price");
        TextField typeF  = field("Type (Food / Decor...)");
        Label     msgLbl = new Label("");

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
                typeF.setText(s.getType());
            }
        });

        Button addBtn = btn("➕  Add",    Theme.goldButtonStyle() + "-fx-font-size:13px; -fx-padding:8 14;");
        Button updBtn = btn("✏️  Update", Theme.roseButtonStyle());
        Button delBtn = btn("🗑️  Delete", Theme.dangerButtonStyle());

        addBtn.setOnAction(e -> {
            try {
                ServiceController.add(nameF.getText(),         // ← Controller
                    Double.parseDouble(priceF.getText()), typeF.getText());
                ok(msgLbl, "✅ Service added!"); reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        updBtn.setOnAction(e -> {
            Service sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a service"); return; }
            ServiceController.update(sel.getId(), nameF.getText(), // ← Controller
                Double.parseDouble(priceF.getText()), typeF.getText());
            ok(msgLbl, "✅ Updated!"); reload(table);
        });

        delBtn.setOnAction(e -> {
            Service sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a service"); return; }
            ServiceController.delete(sel.getId());                 // ← Controller
            err(msgLbl, "🗑️ Deleted!"); reload(table);
        });

        VBox form = new VBox(12);
        form.setPadding(new Insets(24)); form.setPrefWidth(280);
        form.setStyle(Theme.cardStyle());
        Label ft = new Label("Add / Edit Service"); ft.setStyle(Theme.titleStyle(15));
        form.getChildren().addAll(ft, nameF, priceF, typeF, new HBox(8, addBtn, updBtn, delBtn), msgLbl);

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

    private static Button btn(String t, String s) { Button b = new Button(t); b.setStyle(s); return b; }
    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }
}