package views;

import controllers.CustomerController;
import hall_wedding.*;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class CustomerView {

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        root.setPrefSize(960, 580);
        root.setTop(HallView.topBar("👥  Customer Management", "admin"));

        TextField nameF  = field("Full Name");
        TextField emailF = field("Email");
        TextField passF  = field("Password");
        TextField phoneF = field("Phone");
        TextField ssnF   = field("SSN");
        Label     msgLbl = new Label("");

        TableView<Customer> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
            col("ID",    "id",    60),
            col("Name",  "name",  160),
            col("Email", "email", 180),
            col("Phone", "phone", 100)
        );
        reload(table);

        table.getSelectionModel().selectedItemProperty().addListener((o, old, c) -> {
            if (c != null) {
                nameF.setText(c.getName()); emailF.setText(c.getEmail());
                phoneF.setText(String.valueOf(c.getPhone())); ssnF.setText(c.getSsn());
            }
        });

        Button addBtn = btn("➕  Add",    Theme.goldButtonStyle() + "-fx-font-size:13px; -fx-padding:8 14;");
        Button updBtn = btn("✏️  Update", Theme.roseButtonStyle());
        Button delBtn = btn("🗑️  Delete", Theme.dangerButtonStyle());

        addBtn.setOnAction(e -> {
            try {
                CustomerController.add(                         // ← Controller
                    nameF.getText(), emailF.getText(), passF.getText(),
                    Integer.parseInt(phoneF.getText()), ssnF.getText());
                ok(msgLbl, "✅ Customer added!"); reload(table);
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        updBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a customer"); return; }
            CustomerController.update(sel.getId(),              // ← Controller
                nameF.getText(), emailF.getText(),
                Integer.parseInt(phoneF.getText()));
            ok(msgLbl, "✅ Updated!"); reload(table);
        });

        delBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select"); return; }
            CustomerController.delete(sel.getId());             // ← Controller
            err(msgLbl, "🗑️ Deleted!"); reload(table);
        });

        VBox form = new VBox(12);
        form.setPadding(new Insets(24)); form.setPrefWidth(290);
        form.setStyle(Theme.cardStyle());
        Label ft = new Label("Add / Edit Customer"); ft.setStyle(Theme.titleStyle(15));
        form.getChildren().addAll(ft, nameF, emailF, passF, phoneF, ssnF,
            new HBox(8, addBtn, updBtn, delBtn), msgLbl);

        HBox center = new HBox(20); center.setPadding(new Insets(20));
        HBox.setHgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(form, table);
        root.setCenter(center);
        return new Scene(root, 960, 580);
    }

    private static void reload(TableView<Customer> t) {
        t.setItems(FXCollections.observableArrayList(CustomerController.getAll()));
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