package views;

import controllers.CustomerController;
import controllers.BookingController;
import hall_wedding.*;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class CustomerView {

    private static VBox formNode;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NAME_REGEX = "^[\\p{L} ]+$";
    private static final String PHONE_REGEX = "^(010|011|012|015)\\d{8}$";
    private static final String DIGITS_REGEX = "^\\d+$";

    // Page-specific palette (used locally in CustomerView)
    private static final String BG          = "#ECE8F2";
    private static final String CARD_BG     = "rgba(255,255,255,0.62)";
    private static final String GOLD        = "#B8893B";
    private static final String GOLD_LIGHT  = "#D8B77B";
    private static final String DARK        = "#1F2940";
    private static final String TEXT        = "#2C3348";
    private static final String MUTED       = "#7E8093";
    private static final String BORDER      = "rgba(191,201,228,0.85)";
    private static final String BTN_DARK    = "#4B425D";
    private static final String BTN_UPDATE  = "#EAE4F2";
    private static final String SUCCESS     = "#4CAF50";
    private static final String ERROR       = "#D32F2F";
    private static final String ROW_ALT     = "rgba(255,255,255,0.25)";
    private static final String TAB_BG      = "transparent";

    public static Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + ";" + Theme.pageBackgroundStyle());
        root.setPrefSize(1280, 900);
        root.setTop(topBar());

        TextField nameF  = field("Full Name");
        TextField emailF = field("Email");
        TextField passF  = field("Password");
        TextField phoneF = field("Phone");
        TextField ssnF   = field("SSN");
        Label     msgLbl = new Label("");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(Double.MAX_VALUE);
        msgLbl.setPrefWidth(320);
        Label nameErr  = errorLabel();
        Label emailErr = errorLabel();
        Label passErr  = errorLabel();
        Label phoneErr = errorLabel();
        Label ssnErr   = errorLabel();

        TableView<Customer> table = new TableView<>();
        table.setStyle("-fx-background-color:" + CARD_BG + ";" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:14;" +
            "-fx-background-radius:14;" +
            "-fx-table-cell-border-color: transparent;" +
            "-fx-table-header-border-color: transparent;" +
            "-fx-effect: dropshadow(gaussian, rgba(111,123,169,0.12), 12, 0.15, 0, 3);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(320);
        table.setMinHeight(0);
        table.setMaxHeight(Double.MAX_VALUE);
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
                passF.setText(c.getPassword());
                phoneF.setText(c.getPhone()); ssnF.setText(c.getSsn());
            }
        });

        Label bookingAdminLbl = new Label("Select customer to view booking admin");
        bookingAdminLbl.setWrapText(true);
        bookingAdminLbl.setMaxWidth(Double.MAX_VALUE);
        bookingAdminLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + Theme.CREAM + ";");

        table.getSelectionModel().selectedItemProperty().addListener((o, old, c) -> {
            if (c == null) {
                bookingAdminLbl.setText("Select customer to view booking admin");
                return;
            }
            bookingAdminLbl.setText(buildCustomerBookingDetailsText(c));
        });

        Button addBtn = goldButton("Add Customer");
        Button updBtn = updateButton("Update Customer");
        Button delBtn = darkButton("Delete Customer");

        addBtn.setOnAction(e -> {
            try {
                clearErrors(nameErr, emailErr, passErr, phoneErr, ssnErr);
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                String password = passF.getText().trim();
                String phoneTxt = phoneF.getText().trim();
                String ssn = ssnF.getText().trim();
                boolean hasErrors = false;

                if (name.isEmpty()) { showError(nameErr, "Name is required"); hasErrors = true; }
                if (name.length() < 3) { showError(nameErr, "Name must be at least 3 characters"); hasErrors = true; }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    showError(nameErr, "Name must contain letters only"); hasErrors = true;
                }
                if (email.isEmpty()) { showError(emailErr, "Email is required"); hasErrors = true; }
                if (!email.isEmpty() && !email.matches(EMAIL_REGEX)) { showError(emailErr, "Invalid email format"); hasErrors = true; }
                if (password.isEmpty()) { showError(passErr, "Password is required"); hasErrors = true; }
                if (!password.isEmpty() && password.length() < 4) { showError(passErr, "Password must be at least 4 characters"); hasErrors = true; }
                if (phoneTxt.isEmpty()) { showError(phoneErr, "Phone is required"); hasErrors = true; }
                if (!phoneTxt.isEmpty() && !phoneTxt.matches(PHONE_REGEX)) {
                    showError(phoneErr, "Phone must be 11 digits and start with 010, 011, 012, or 015"); hasErrors = true;
                }
                if (ssn.isEmpty()) { showError(ssnErr, "SSN is required"); hasErrors = true; }
                if (!ssn.isEmpty() && (!ssn.matches(DIGITS_REGEX) || ssn.length() != 14)) {
                    showError(ssnErr, "SSN must be exactly 14 digits"); hasErrors = true;
                }

                if (hasErrors) return;

                CustomerController.add(                         // ← Controller
                    name, email, password,
                    phoneTxt, ssn);
                ok(msgLbl, "✅ Customer added!"); reload(table);
                clearErrors(nameErr, emailErr, passErr, phoneErr, ssnErr);
                nameF.clear();
                emailF.clear();
                passF.clear();
                phoneF.clear();
                ssnF.clear();
            } catch (Exception ex) { err(msgLbl, ex.getMessage()); }
        });

        updBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "Select a customer"); return; }
            try {
                clearErrors(nameErr, emailErr, passErr, phoneErr, ssnErr);
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                String password = passF.getText().trim();
                String phoneTxt = phoneF.getText().trim();
                String ssn = ssnF.getText().trim();

                boolean hasErrors = false;
                if (name.isEmpty()) { showError(nameErr, "Name is required"); hasErrors = true; }
                if (name.length() < 3) { showError(nameErr, "Name must be at least 3 characters"); hasErrors = true; }
                if (!name.matches(NAME_REGEX) || name.replace(" ", "").isEmpty()) {
                    showError(nameErr, "Name must contain letters only"); hasErrors = true;
                }
                if (email.isEmpty()) { showError(emailErr, "Email is required"); hasErrors = true; }
                if (!email.isEmpty() && !email.matches(EMAIL_REGEX)) { showError(emailErr, "Invalid email format"); hasErrors = true; }
                if (password.isEmpty()) { showError(passErr, "Password is required"); hasErrors = true; }
                if (!password.isEmpty() && password.length() < 4) { showError(passErr, "Password must be at least 4 characters"); hasErrors = true; }
                if (phoneTxt.isEmpty()) { showError(phoneErr, "Phone is required"); hasErrors = true; }
                if (!phoneTxt.isEmpty() && !phoneTxt.matches(PHONE_REGEX)) {
                    showError(phoneErr, "Phone must be 11 digits and start with 010, 011, 012, or 015"); hasErrors = true;
                }
                if (ssn.isEmpty()) { showError(ssnErr, "SSN is required"); hasErrors = true; }
                if (!ssn.isEmpty() && (!ssn.matches(DIGITS_REGEX) || ssn.length() != 14)) {
                    showError(ssnErr, "SSN must be exactly 14 digits"); hasErrors = true;
                }

                if (hasErrors) return;

                CustomerController.update(sel.getId(),              // ← Controller
                    name, email, password,
                    phoneTxt, ssn);
                ok(msgLbl, "✅ Updated!");
                reload(table);
                table.refresh();
            } catch (Exception ex) {
                err(msgLbl, ex.getMessage());
            }
        });

        delBtn.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { err(msgLbl, "Select"); return; }
            CustomerController.delete(sel.getId());             // ← Controller
            err(msgLbl, "🗑️ Deleted!"); reload(table);
            nameF.clear();
            emailF.clear();
            passF.clear();
            phoneF.clear();
            ssnF.clear();
        });

        VBox form = new VBox(12);
        formNode = form;
        form.setPadding(new Insets(24)); form.setPrefWidth(360);
        form.setStyle("-fx-background-color:" + CARD_BG + ";" +
            "-fx-background-radius:25;" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-radius:25;" +
            "-fx-border-width:1;" +
            "-fx-effect: dropshadow(gaussian, rgba(111,123,169,0.12), 16, 0.15, 0, 4);");
        Label ft = new Label("Add / Edit Customer"); ft.setStyle(Theme.titleStyle(15));
        VBox actions = new VBox(8, addBtn, updBtn, delBtn);
        form.getChildren().addAll(ft, msgLbl,
            labeledWithError("Full Name", nameF, nameErr),
            labeledWithError("Email", emailF, emailErr),
            labeledWithError("Password", passF, passErr),
            labeledWithError("Phone", phoneF, phoneErr),
            labeledWithError("SSN", ssnF, ssnErr),
            actions);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");
        formScroll.setPrefWidth(360);
        formScroll.setMinWidth(340);

        // ── Search Card ───────────────────────────────────
        TextField searchF = field("Search by name or phone");
        Button searchBtn = goldButton("🔍 Search");
        searchBtn.setPrefWidth(170);
        searchBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button resetBtn = new Button("↺ Reset");
        resetBtn.setStyle("-fx-background-color:#E2A76F; -fx-text-fill:#FFFFFF; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-radius:18; -fx-padding:10 0; -fx-cursor:hand;");
        resetBtn.setPrefWidth(120);
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        
        Label searchMsgLbl = new Label("");
        searchMsgLbl.setStyle("-fx-font-size:11px;");
        searchMsgLbl.setMaxWidth(Double.MAX_VALUE);
        
        searchBtn.setOnAction(e -> {
            try {
                String searchTerm = searchF.getText().trim();
                List<Customer> results = CustomerController.searchByNameOrPhone(searchTerm);
                table.setItems(FXCollections.observableArrayList(results));
                searchMsgLbl.setText("Found " + results.size() + " customer(s)");
                searchMsgLbl.setStyle("-fx-font-size:11px; -fx-text-fill:" + Theme.SUCCESS + ";");
            } catch (Exception ex) {
                searchMsgLbl.setText(ex.getMessage());
                searchMsgLbl.setStyle("-fx-font-size:11px; -fx-text-fill:" + Theme.ERROR + ";");
            }
        });
        
        resetBtn.setOnAction(e -> {
            searchF.clear();
            searchMsgLbl.setText("");
            reload(table);
            bookingAdminLbl.setText("Select customer to view booking admin");
        });
        
        HBox searchRow = new HBox(12, new Label("Search"), searchF);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchF, Priority.ALWAYS);

        HBox buttonRow = new HBox(10, searchBtn, resetBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        VBox searchCard = new VBox(10);
        searchCard.setPadding(new Insets(14));
        searchCard.setStyle("-fx-background-color:" + CARD_BG + "; -fx-background-radius:25; -fx-border-color:" + BORDER + "; -fx-border-radius:25; -fx-border-width:1;");
        Label searchTitle = new Label("Search Customer");
        searchTitle.setStyle(Theme.titleStyle(16));
        searchCard.getChildren().addAll(searchTitle, searchRow, buttonRow, searchMsgLbl);
        
        VBox adminInfoCard = new VBox(8);
        adminInfoCard.setPadding(new Insets(12));
        adminInfoCard.setStyle("-fx-background-color:" + CARD_BG + "; -fx-background-radius:25; -fx-border-color:" + BORDER + "; -fx-border-radius:25; -fx-border-width:1;");
        Label adminInfoTitle = new Label("Booking Details");
        adminInfoTitle.setStyle(Theme.titleStyle(14));

        ScrollPane adminInfoScroll = new ScrollPane(bookingAdminLbl);
        adminInfoScroll.setFitToWidth(true);
        adminInfoScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        adminInfoScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        adminInfoScroll.setPrefViewportHeight(120);
        adminInfoScroll.setMinHeight(120);
        adminInfoScroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");
        adminInfoLblStyle(bookingAdminLbl);

        adminInfoCard.getChildren().addAll(adminInfoTitle, adminInfoScroll);

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color:" + CARD_BG + "; -fx-background-radius:25; -fx-border-color:" + BORDER + "; -fx-border-radius:25; -fx-border-width:1;");
        tableCard.getChildren().add(table);

        VBox tableSection = new VBox(12, searchCard, adminInfoCard, tableCard);
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox center = new HBox(20);
        center.setPadding(new Insets(20));
        center.setFillHeight(true);
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        center.getChildren().addAll(formScroll, tableSection);
        root.setCenter(center);
        return new Scene(root, 960, 580);
    }

    private static void reload(TableView<Customer> t) {
        t.setItems(FXCollections.observableArrayList(CustomerController.getAll()));
    }

    private static String buildCustomerBookingDetailsText(Customer customer) {
        if (customer == null || customer.getId() == null) {
            return "No customer selected";
        }

        List<Booking> bookings = BookingController.getByCustomer(customer.getId());
        if (bookings.isEmpty()) {
            return "No bookings for this customer";
        }

        List<String> bookingLines = bookings.stream()
            .map(booking -> {
                Admin admin = booking.getCreatedBy();
                String adminName = "Unknown Admin";
                if (admin != null) {
                    if (admin.getName() != null && !admin.getName().isBlank()) {
                        adminName = admin.getName();
                    } else if (admin.getEmail() != null && !admin.getEmail().isBlank()) {
                        adminName = admin.getEmail();
                    }
                }
                String bookingId = booking.getId() != null ? String.valueOf(booking.getId()) : "N/A";
                String services = booking.getServices() == null || booking.getServices().isEmpty()
                    ? "No services"
                    : booking.getServices().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(Service::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .distinct()
                        .collect(Collectors.joining(", "));
                if (services.isBlank()) {
                    services = "No services";
                }

                return "• Booking #" + bookingId + " | Admin: " + adminName + " | Services: " + services;
            })
            .collect(Collectors.toList());

        if (bookingLines.isEmpty()) {
            return "Bookings exist but no admin assigned";
        }

        return "Booking Details (" + bookingLines.size() + ")\n" + String.join("\n", bookingLines);
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

    private static VBox labeledWithError(String title, Node field, Label errorLabel) {
        Label label = new Label(title);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        return new VBox(3, label, field, errorLabel);
    }

    private static void adminInfoLblStyle(Label label) {
        label.setStyle("-fx-font-size:12px; -fx-text-fill:" + Theme.CREAM + "; -fx-padding:2 0 0 0;");
    }

    private static HBox topBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 24, 12, 16));
        bar.setStyle("-fx-background-color: rgba(255,255,255,0.45); -fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0;");
        Button back = new Button("← Back");
        back.setStyle("-fx-background-color:" + GOLD + "; -fx-text-fill:#FFFFFF; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-radius:18; -fx-padding:8 18; -fx-cursor:hand;");
        back.setOnAction(e -> SceneManager.switchTo("admin"));
        Label lbl = new Label("👥  Customer Management");
        lbl.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-font-family:'Georgia'; -fx-text-fill:" + DARK + ";");
        bar.getChildren().addAll(back, lbl);
        return bar;
    }

    private static Button goldButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:" + GOLD + "; -fx-text-fill:#FFFFFF; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-radius:18; -fx-padding:10 0; -fx-effect: dropshadow(gaussian, rgba(184,137,59,0.22), 12, 0.2, 0, 3); -fx-cursor:hand;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color:" + GOLD_LIGHT + "; -fx-text-fill:#FFFFFF; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-radius:18; -fx-padding:10 0; -fx-effect: dropshadow(gaussian, rgba(184,137,59,0.22), 12, 0.2, 0, 3); -fx-cursor:hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color:" + GOLD + "; -fx-text-fill:#FFFFFF; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-radius:18; -fx-padding:10 0; -fx-effect: dropshadow(gaussian, rgba(184,137,59,0.22), 12, 0.2, 0, 3); -fx-cursor:hand;"));
        return button;
    }

    private static Button updateButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:" + BTN_UPDATE + "; -fx-text-fill:" + DARK + "; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-color: rgba(255,255,255,0.55); -fx-border-width:1; -fx-border-radius:18; -fx-padding:10 0; -fx-cursor:hand;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private static Button darkButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:" + BTN_DARK + "; -fx-text-fill:#FFFFFF; -fx-font-size:13px; -fx-font-family:'Georgia'; -fx-font-weight:bold; -fx-background-radius:18; -fx-border-radius:18; -fx-padding:10 0; -fx-cursor:hand;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private static Button btn(String t, String s) {
        Button b = new Button(t);
        b.setStyle(s);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }
    private static Label errorLabel() {
        Label label = new Label("");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size:11px; -fx-text-fill:" + Theme.ERROR + ";");
        return label;
    }
    private static void clearErrors(Label... labels) {
        javafx.application.Platform.runLater(() -> {
            for (Label label : labels) label.setText("");
        });
    }
    private static void showError(Label label, String msg) {
        javafx.application.Platform.runLater(() -> {
            label.setStyle("-fx-font-size:11px; -fx-text-fill:" + Theme.ERROR + ";");
            label.setText(msg);
            if (formNode != null) formNode.requestLayout();
        });
    }
    private static void ok(Label l, String m)  { l.setStyle("-fx-text-fill:" + Theme.SUCCESS + "; -fx-font-size:12px;"); l.setText(m); }
    private static void err(Label l, String m) { l.setStyle("-fx-text-fill:" + Theme.ERROR   + "; -fx-font-size:12px;"); l.setText(m); }
}