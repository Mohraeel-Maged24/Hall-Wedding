package views;

import controllers.CustomerController;
import hall_wedding.*;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class CustomerView {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NAME_REGEX = "^[\\p{L} ]+$";
    private static final String PHONE_REGEX = "^(010|011|012|015)\\d{8}$";
    private static final String DIGITS_REGEX = "^\\d+$";

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
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setPrefWidth(320);

        TableView<Customer> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
            col("ID",    "id",    60),
            col("Name",  "name",  160),
            col("Email", "email", 180),
            col("Phone", "phone", 140),
            col("SSN",   "ssn",   140)
        );
        reload(table);

        table.getSelectionModel().selectedItemProperty().addListener((o, old, c) -> {
            if (c != null) {
                nameF.setText(c.getName()); emailF.setText(c.getEmail());
                phoneF.setText(c.getPhone()); ssnF.setText(c.getSsn());
            }
        });

        Button addBtn = btn("Add Customer",    Theme.goldButtonStyle() + "-fx-font-size:13px; -fx-padding:8 14;");
        Button updBtn = btn("Update Customer", Theme.roseButtonStyle());
        Button delBtn = btn("Delete Customer", Theme.dangerButtonStyle());

        addBtn.setOnAction(e -> {
            try {
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                String password = passF.getText().trim();
                String phoneTxt = phoneF.getText().trim();
                String ssn = ssnF.getText().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phoneTxt.isEmpty() || ssn.isEmpty()) {
                    err(msgLbl, "⚠️ All fields are required");
                    return;
                }
                if (name.length() < 3) {
                    err(msgLbl, "⚠️ Name must be at least 3 characters");
                    return;
                }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    err(msgLbl, "⚠️ Name must contain letters only");
                    return;
                }
                if (!email.matches(EMAIL_REGEX)) {
                    err(msgLbl, "⚠️ Invalid email format");
                    return;
                }
                if (password.length() < 4) {
                    err(msgLbl, "⚠️ Password must be at least 4 characters");
                    return;
                }
                if (!phoneTxt.matches(PHONE_REGEX)) {
                    err(msgLbl, "⚠️ Phone must be 11 digits and start with 010, 011, 012, or 015");
                    return;
                }
                if (!ssn.matches(DIGITS_REGEX) || ssn.length() != 14) {
                    err(msgLbl, "⚠️ SSN must be exactly 14 digits");
                    return;
                }

                CustomerController.add(                         // ← Controller
                    name, email, password,
                    phoneTxt, ssn);
                ok(msgLbl, "✅ Customer added!"); reload(table);
                nameF.clear();
                emailF.clear();
                passF.clear();
                phoneF.clear();
                ssnF.clear();
            } catch (Exception ex) { err(msgLbl, "❌ " + ex.getMessage()); }
        });

        updBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select a customer"); return; }
            try {
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                String phoneTxt = phoneF.getText().trim();

                if (name.isEmpty() || email.isEmpty() || phoneTxt.isEmpty()) {
                    err(msgLbl, "⚠️ Name, email and phone are required");
                    return;
                }
                if (name.length() < 3) {
                    err(msgLbl, "⚠️ Name must be at least 3 characters");
                    return;
                }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    err(msgLbl, "⚠️ Name must contain letters only");
                    return;
                }
                if (!email.matches(EMAIL_REGEX)) {
                    err(msgLbl, "⚠️ Invalid email format");
                    return;
                }
                if (!phoneTxt.matches(PHONE_REGEX)) {
                    err(msgLbl, "⚠️ Phone must be 11 digits and start with 010, 011, 012, or 015");
                    return;
                }

                CustomerController.update(sel.getId(),              // ← Controller
                    name, email,
                    phoneTxt);
                ok(msgLbl, "✅ Updated!"); reload(table);
            } catch (Exception ex) {
                err(msgLbl, "❌ " + ex.getMessage());
            }
        });

        delBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "⚠️ Select"); return; }
            CustomerController.delete(sel.getId());             // ← Controller
            err(msgLbl, "🗑️ Deleted!"); reload(table);
            nameF.clear();
            emailF.clear();
            passF.clear();
            phoneF.clear();
            ssnF.clear();
        });

        VBox form = new VBox(12);
        form.setPadding(new Insets(24)); form.setPrefWidth(360);
        form.setStyle(Theme.cardStyle());
        Label ft = new Label("Add / Edit Customer"); ft.setStyle(Theme.titleStyle(15));
        VBox actions = new VBox(8, addBtn, updBtn, delBtn);
        form.getChildren().addAll(ft,
            labeled("Full Name", nameF),
            labeled("Email", emailF),
            labeled("Password", passF),
            labeled("Phone", phoneF),
            labeled("SSN", ssnF),
            actions, msgLbl);

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