package views;

import controllers.ServiceController;
import hall_wedding.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class ServiceView {

    private static VBox formNode;

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    // ── Colour tokens: identical to HallView ─────────────────────────────
    private static final String BG         = "#ECE8F2";
    private static final String CARD_BG    = "rgba(255,255,255,0.62)";
    private static final String GOLD       = "#B8893B";
    private static final String GOLD_LIGHT = "#D8B77B";
    private static final String DARK       = "#1F2940";
    private static final String TEXT       = "#2C3348";
    private static final String MUTED      = "#7E8093";
    private static final String BORDER     = "rgba(191,201,228,0.85)";
    private static final String BTN_DARK   = "#4B425D";
    private static final String SUCCESS    = "#4CAF50";
    private static final String ERROR      = "#D32F2F";
    private static final String TAB_BG     = "transparent";

    private static final ObservableList<String> SERVICE_TYPES = FXCollections.observableArrayList(
        "Food", "Decor", "Photography", "Video", "Music", "Lighting",
        "Flower", "Cake", "Cars", "Security", "Coordination", "Other"
    );

    // ── Shared table reference ─────────────────────────────────────────────
    private static TableView<Service> sharedTableRef;

    // ═══════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═══════════════════════════════════════════════════════════════════════
    public static Scene build() {
        double width  = Hall_Wedding.primaryStage != null ? Hall_Wedding.primaryStage.getWidth()  : 1280;
        double height = Hall_Wedding.primaryStage != null ? Hall_Wedding.primaryStage.getHeight() : 960;

        StackPane root = new StackPane();
        root.setStyle(Theme.pageBackgroundStyle());

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(18));
        layout.setStyle("-fx-background-color: transparent;");
        layout.setTop(buildTopBar());
        layout.setCenter(buildBody());

        root.getChildren().add(layout);
        return new Scene(root, width, height);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TOP BAR  — mirrors HallView top bar
    // ═══════════════════════════════════════════════════════════════════════
    private static HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 16));
        bar.setPrefHeight(56);
        bar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.58);" +
            "-fx-background-radius:22 22 0 0;" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:22 22 0 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(108,108,134,0.12), 18, 0.2, 0, 4);"
        );

        // Back button
        Button back = new Button("← Back");
        back.setStyle(
            "-fx-background-color:" + GOLD + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-border-color: rgba(255,255,255,0.45);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:14;" +
            "-fx-background-radius:14;" +
            "-fx-padding:6 14;" +
            "-fx-cursor:hand;"
        );
        back.setOnAction(e -> SceneManager.switchTo("admin"));
        back.setOnMouseEntered(e -> back.setStyle(
            "-fx-background-color:" + GOLD_LIGHT + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-border-color: rgba(255,255,255,0.5);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:14;" +
            "-fx-background-radius:14;" +
            "-fx-padding:6 14;" +
            "-fx-cursor:hand;"
        ));
        back.setOnMouseExited(e -> back.setStyle(
            "-fx-background-color:" + GOLD + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-border-color: rgba(255,255,255,0.45);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:14;" +
            "-fx-background-radius:14;" +
            "-fx-padding:6 14;" +
            "-fx-cursor:hand;"
        ));

        Label icon  = new Label("🛎️");
        icon.setStyle("-fx-font-size:20px;");

        Label title = new Label("SERVICE MANAGEMENT");
        title.setStyle(
            "-fx-font-size:20px;" +
            "-fx-font-weight:bold;" +
            "-fx-font-family:'Georgia';" +
            "-fx-text-fill:" + DARK + ";"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label version = new Label("Wedding Hall System v1.0");
        version.setStyle("-fx-font-size:12px; -fx-text-fill:" + MUTED + ";");

        bar.setSpacing(12);
        bar.getChildren().addAll(back, icon, title, spacer, version);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BODY — left form panel + right tabs panel
    // ═══════════════════════════════════════════════════════════════════════
    private static HBox buildBody() {
        HBox body = new HBox(16);
        body.setFillHeight(true);

        VBox leftPanel  = buildFormPanel();
        VBox rightPanel = buildRightPanel();

        leftPanel.setPrefWidth(300);
        leftPanel.setMinWidth(280);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        body.getChildren().addAll(leftPanel, rightPanel);
        return body;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LEFT — Add / Edit Service form
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildFormPanel() {
        TextField nameF  = formField("Service Name");
        TextField priceF = formField("Price");
        ComboBox<String> typeF = formCombo("Type", SERVICE_TYPES);

        Label msgLbl   = msgLabel();
        Label nameErr  = errLabel();
        Label priceErr = errLabel();
        Label typeErr  = errLabel();

        Button addBtn    = goldButton("＋  Add Service");
        Button updateBtn = greyButton("↻  Update Service");
        Button deleteBtn = darkButton("🗑  Delete Service");

        // ── Add ──
        addBtn.setOnAction(e -> {
            clearErrs(nameErr, priceErr, typeErr);
            msgLbl.setText("");
            String name     = nameF.getText().trim();
            String priceTxt = priceF.getText().trim();
            String type     = typeF.getValue();
            boolean bad = false;

            if (name.isEmpty())                           { showErr(nameErr, "Service name is required"); bad = true; }
            else if (name.length() < 3)                   { showErr(nameErr, "Min 3 characters"); bad = true; }
            else if (!name.matches(NAME_REGEX))            { showErr(nameErr, "Letters only"); bad = true; }
            if (priceTxt.isEmpty())                       { showErr(priceErr, "Price is required"); bad = true; }
            if (type == null)                             { showErr(typeErr, "Type is required"); bad = true; }

            Double price = null;
            if (!priceTxt.isEmpty()) {
                try { price = Double.parseDouble(priceTxt); }
                catch (NumberFormatException ex) { showErr(priceErr, "Must be a number"); bad = true; }
            }
            if (price != null && price <= 0) { showErr(priceErr, "Must be > 0"); bad = true; }
            if (bad) return;

            try {
                ServiceController.add(name, price, type);
                ok(msgLbl, "✅ Service added!");
                nameF.clear(); priceF.clear(); typeF.setValue(null);
                reloadShared();
            } catch (Exception ex) { err(msgLbl, ex.getMessage()); }
        });

        // ── Update ──
        updateBtn.setOnAction(e -> {
            if (sharedTableRef == null || sharedTableRef.getSelectionModel().getSelectedItem() == null) {
                err(msgLbl, "Select a service first"); return;
            }
            Service sel = sharedTableRef.getSelectionModel().getSelectedItem();
            clearErrs(nameErr, priceErr, typeErr);
            msgLbl.setText("");
            String name     = nameF.getText().trim();
            String priceTxt = priceF.getText().trim();
            String type     = typeF.getValue();
            boolean bad = false;

            if (name.isEmpty())                           { showErr(nameErr, "Service name is required"); bad = true; }
            else if (name.length() < 3)                   { showErr(nameErr, "Min 3 characters"); bad = true; }
            else if (!name.matches(NAME_REGEX))            { showErr(nameErr, "Letters only"); bad = true; }
            if (priceTxt.isEmpty())                       { showErr(priceErr, "Price is required"); bad = true; }
            if (type == null)                             { showErr(typeErr, "Type is required"); bad = true; }

            Double price = null;
            if (!priceTxt.isEmpty()) {
                try { price = Double.parseDouble(priceTxt); }
                catch (NumberFormatException ex) { showErr(priceErr, "Must be a number"); bad = true; }
            }
            if (price != null && price <= 0) { showErr(priceErr, "Must be > 0"); bad = true; }
            if (bad) return;

            try {
                ServiceController.update(sel.getId(), name, price, type);
                ok(msgLbl, "✅ Updated!");
                reloadShared();
                sharedTableRef.refresh();
            } catch (Exception ex) { err(msgLbl, ex.getMessage()); }
        });

        // ── Delete ──
        deleteBtn.setOnAction(e -> {
            if (sharedTableRef == null || sharedTableRef.getSelectionModel().getSelectedItem() == null) {
                err(msgLbl, "Select a service first"); return;
            }
            Service sel = sharedTableRef.getSelectionModel().getSelectedItem();
            try {
                ServiceController.delete(sel.getId());
                err(msgLbl, "🗑 Deleted");
                reloadShared();
            } catch (Exception ex) { err(msgLbl, ex.getMessage()); }
        });

        // ── Build form VBox ──
        VBox form = new VBox(0);
        formNode = form;
        form.setStyle(
            "-fx-background-color:" + CARD_BG + ";" +
            "-fx-background-radius:22;" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:22;" +
            "-fx-effect: dropshadow(gaussian, rgba(112,112,146,0.14), 18, 0.2, 0, 6);"
        );
        form.setPadding(new Insets(24, 24, 24, 24));

        Label formTitle = new Label("✎  Add / Edit Service");
        formTitle.setStyle(
            "-fx-font-size:16px;" +
            "-fx-font-weight:bold;" +
            "-fx-font-family:'Georgia';" +
            "-fx-text-fill:" + DARK + ";" +
            "-fx-padding:0 0 16 0;"
        );

        form.getChildren().addAll(
            formTitle,
            msgLbl,
            fieldBlock("SERVICE NAME", nameF,  nameErr),
            fieldBlock("PRICE (EGP)",  priceF, priceErr),
            fieldBlock("SERVICE TYPE", typeF,  typeErr),
            spacer(16),
            addBtn,
            spacer(8),
            updateBtn,
            spacer(8),
            deleteBtn
        );

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        VBox wrapper = new VBox(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        wrapper.setStyle("-fx-background-color: transparent;");

        // Store field refs on the form node for table-selection auto-fill
        form.getProperties().put("nameF",  nameF);
        form.getProperties().put("typeF",  typeF);
        form.getProperties().put("priceF", priceF);

        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  RIGHT — TabPane: Search Service | Analytics
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildRightPanel() {
        sharedTableRef = buildTable();
        reloadTable(sharedTableRef);

        Tab searchTab    = new Tab("🔍  Search Service", buildSearchContent(sharedTableRef));
        Tab analyticsTab = new Tab("📊  Analytics",      wrapScroll(buildAnalyticsContent()));
        searchTab.setClosable(false);
        analyticsTab.setClosable(false);

        TabPane tabs = new TabPane(searchTab, analyticsTab);
        tabs.setStyle(
            "-fx-background-color:" + TAB_BG + ";" +
            "-fx-tab-min-width:150px;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-tab-max-height:34px;"
        );

        VBox panel = new VBox(tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        panel.setStyle("-fx-background-color: transparent;");
        panel.setPadding(new Insets(0));
        return panel;
    }

    // ── Search tab content ─────────────────────────────────────────────────
    private static Node buildSearchContent(TableView<Service> table) {
        TextField searchNameF         = formField("Search by name");
        ComboBox<String> searchTypeF  = formCombo("Filter by type", SERVICE_TYPES);
        Label searchMsg               = msgLabel();

        Button searchBtn = goldButton("🔍  Search");
        Button resetBtn  = outlineButton("↺  Reset");
        searchBtn.setPrefWidth(150);
        resetBtn.setPrefWidth(150);

        searchBtn.setOnAction(e -> {
            try {
                String nm = searchNameF.getText().trim();
                String tp = searchTypeF.getValue();

                List<Service> res;
                if (tp != null && !tp.isEmpty()) {
                    // Filter by type (and optionally name)
                    res = ServiceController.searchByNameOrType(tp);
                    if (!nm.isEmpty()) {
                        String lower = nm.toLowerCase();
                        res = res.stream()
                            .filter(s -> s.getName().toLowerCase().contains(lower))
                            .collect(java.util.stream.Collectors.toList());
                    }
                } else {
                    res = ServiceController.searchByNameOrType(nm.isEmpty() ? null : nm);
                }
                table.setItems(FXCollections.observableArrayList(res));
                ok(searchMsg, "Found " + res.size() + " service(s)");
            } catch (Exception ex) { err(searchMsg, ex.getMessage()); }
        });

        resetBtn.setOnAction(e -> {
            searchNameF.clear(); searchTypeF.setValue(null); searchMsg.setText("");
            reloadTable(table);
        });

        // Auto-fill form on row selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null && formNode != null) {
                TextField nf = (TextField) formNode.getProperties().get("nameF");
                TextField pf = (TextField) formNode.getProperties().get("priceF");
                ComboBox<String> tf = (ComboBox<String>) formNode.getProperties().get("typeF");
                if (nf != null) nf.setText(sel.getName());
                if (pf != null) pf.setText(String.valueOf(sel.getPrice()));
                if (tf != null) tf.setValue(sel.getType());
            }
        });

        Label nameLabel = colLabel("NAME");
        Label typeLabel = colLabel("TYPE");

        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        VBox nameBox = new VBox(6, nameLabel, searchNameF);
        VBox typeBox = new VBox(6, typeLabel, searchTypeF);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        filterRow.getChildren().addAll(nameBox, typeBox);

        HBox btnRow = new HBox(14, searchBtn, resetBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        btnRow.setPadding(new Insets(4, 0, 8, 0));

        VBox content = new VBox(0);
        content.setStyle(
            "-fx-background-color:" + CARD_BG + ";" +
            "-fx-background-radius:22;" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:22;"
        );
        content.setPadding(new Insets(20, 24, 20, 24));
        content.setSpacing(0);
        content.getChildren().addAll(filterRow, spacer(14), btnRow, searchMsg, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return content;
    }

    // ── Analytics tab content ──────────────────────────────────────────────
    private static VBox buildAnalyticsContent() {

        // ── 1. Most requested service (Top 1) ─────────────────────────────
        Label topServiceLbl = new Label("—");
        topServiceLbl.setStyle(
            "-fx-font-size:14px; -fx-font-weight:bold;" +
            "-fx-text-fill:" + GOLD + "; -fx-font-family:'Georgia';"
        );
        topServiceLbl.setWrapText(true);

        Button topServiceBtn = goldButton("⭐  Most Requested Service");
        topServiceBtn.setOnAction(e -> {
            Object[] row = ServiceController.getTopRequestedService();
            if (row == null) {
                topServiceLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + MUTED + ";");
                topServiceLbl.setText("No services in bookings yet");
                return;
            }
            topServiceLbl.setStyle(
                "-fx-font-size:14px; -fx-font-weight:bold;" +
                "-fx-text-fill:" + GOLD + "; -fx-font-family:'Georgia';"
            );
            topServiceLbl.setText("⭐ " + row[0] + "  [" + row[1] + "]\n🔢 Requested: " + row[2] + " times");
        });

        // ── 2. Rank all services by request count ─────────────────────────
        Label rankLbl = new Label("—");
        rankLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + TEXT + "; -fx-font-family:'Georgia';");
        rankLbl.setWrapText(true);

        Button rankBtn = goldButton("📊  Rank All Services");
        rankBtn.setOnAction(e -> {
            List<Object[]> rows = ServiceController.getMostRequestedServices();
            if (rows.isEmpty()) { rankLbl.setText("No data yet"); return; }
            StringBuilder sb = new StringBuilder();
            int r = 1;
            for (Object[] row : rows) {
                String medal = r == 1 ? "🥇" : r == 2 ? "🥈" : r == 3 ? "🥉" : "  " + r + ".";
                sb.append(medal).append("  ").append(row[0])
                  .append("  [").append(row[1]).append("]")
                  .append("  —  ").append(row[2]).append("x\n");
                r++;
            }
            rankLbl.setText(sb.toString().trim());
        });

        // ── 3. Price range search ──────────────────────────────────────────
        TextField minPriceF = formField("e.g. 500");
        TextField maxPriceF = formField("e.g. 5000");
        minPriceF.textProperty().addListener((o, ov, nv) -> { if (!nv.matches("\\d*")) minPriceF.setText(nv.replaceAll("[^\\d]", "")); });
        maxPriceF.textProperty().addListener((o, ov, nv) -> { if (!nv.matches("\\d*")) maxPriceF.setText(nv.replaceAll("[^\\d]", "")); });

        Label priceMsg = msgLabel();
        TableView<Service> priceTable = buildTable();
        priceTable.setPrefHeight(170);

        Button priceSearchBtn = goldButton("Search");
        Button priceResetBtn  = outlineButton("Reset");

        priceSearchBtn.setOnAction(e -> {
            String mn = minPriceF.getText().trim(), mx = maxPriceF.getText().trim();
            if (mn.isEmpty() || mx.isEmpty()) { err(priceMsg, "Both values required"); return; }
            double minP, maxP;
            try { minP = Double.parseDouble(mn); maxP = Double.parseDouble(mx); }
            catch (NumberFormatException ex) { err(priceMsg, "Numbers only"); return; }
            if (minP > maxP) { err(priceMsg, "Min > Max"); return; }
            try {
                // Filter services by price range
                List<Service> all = ServiceController.getAll();
                double fMin = minP, fMax = maxP;
                List<Service> res = all.stream()
                    .filter(s -> s.getPrice() >= fMin && s.getPrice() <= fMax)
                    .sorted(java.util.Comparator.comparingDouble(Service::getPrice))
                    .collect(java.util.stream.Collectors.toList());
                priceTable.setItems(FXCollections.observableArrayList(res));
                ok(priceMsg, "Found " + res.size() + " service(s)");
            } catch (Exception ex) { err(priceMsg, ex.getMessage()); }
        });
        priceResetBtn.setOnAction(e -> { minPriceF.clear(); maxPriceF.clear(); priceMsg.setText(""); priceTable.getItems().clear(); });

        // ── 4. Services by type ─────────────────────────────────────────────
        ComboBox<String> typeCountCombo = formCombo("Select Type", SERVICE_TYPES);
        Label typeCountLbl = new Label("—");
        typeCountLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + TEXT + "; -fx-font-family:'Georgia';");
        typeCountLbl.setWrapText(true);

        Button typeCountBtn = outlineButton("Show Services");
        typeCountBtn.setOnAction(e -> {
            String type = typeCountCombo.getValue();
            if (type == null) { typeCountLbl.setText("Select a type first"); return; }
            List<Service> services = ServiceController.getServicesByType(type);
            if (services.isEmpty()) { typeCountLbl.setText("No services of this type"); return; }
            StringBuilder sb = new StringBuilder();
            for (Service s : services) {
                sb.append("• ").append(s.getName())
                  .append(" — ").append(String.format("%.0f EGP", s.getPrice()))
                  .append("\n");
            }
            typeCountLbl.setText(sb.toString().trim());
        });

        VBox analytics = new VBox(14);
        analytics.setPadding(new Insets(20, 24, 24, 24));
        analytics.getChildren().addAll(
            analyticsCard("⭐ Most Requested Service",
                topServiceBtn, topServiceLbl),
            analyticsCard("🏆 All Services Ranking",
                rankBtn, rankLbl),
            analyticsCard("💰 Price Range Search",
                new HBox(12, vBox("Min Price (EGP)", minPriceF), vBox("Max Price (EGP)", maxPriceF)),
                new HBox(8, priceSearchBtn, priceResetBtn),
                priceMsg, priceTable),
            analyticsCard("🔢 Services Count by Type",
                typeCountCombo, typeCountBtn, typeCountLbl)
        );
        return analytics;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SHARED TABLE
    // ═══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static TableView<Service> buildTable() {
        TableView<Service> t = new TableView<>();
        t.setStyle(
            "-fx-background-color: rgba(255,255,255,0.40);" +
            "-fx-background-radius:16;" +
            "-fx-border-color:transparent;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';"
        );
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Service, Integer> idCol    = col("ID",    "id",    60);
        TableColumn<Service, String>  nameCol  = col("NAME",  "name",  200);
        TableColumn<Service, String>  typeCol  = col("TYPE",  "type",  140);
        TableColumn<Service, Double>  priceCol = col("PRICE", "price", 120);

        // Style price column black
        priceCol.setCellFactory(tc -> new TableCell<Service, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill:" + TEXT + "; -fx-font-weight:bold;");
                }
            }
        });

        t.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle(
                        "-fx-background-color:" + GOLD + ";" +
                        "-fx-text-fill:#FFFFFF;" +
                        "-fx-font-family:'Georgia';"
                    );
                } else {
                    setStyle(
                        "-fx-background-color: rgba(255,255,255,0.32);" +
                        "-fx-text-fill:" + TEXT + ";" +
                        "-fx-font-family:'Georgia';"
                    );
                }
            }
        });

        t.getColumns().addAll(idCol, nameCol, typeCol, priceCol);
        t.setPlaceholder(new Label("No services found"));
        return t;
    }

    private static void reloadTable(TableView<Service> t) {
        try { t.setItems(FXCollections.observableArrayList(ServiceController.getAll())); }
        catch (Exception e) { t.setItems(FXCollections.observableArrayList()); }
    }

    private static void reloadShared() {
        if (sharedTableRef != null) reloadTable(sharedTableRef);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SMALL HELPERS — identical signatures to HallView
    // ═══════════════════════════════════════════════════════════════════════

    private static TextField formField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle(
            "-fx-background-color: rgba(255,255,255,0.74);" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:20;" +
            "-fx-background-radius:20;" +
            "-fx-padding:9 14;" +
            "-fx-font-size:13px;" +
            "-fx-text-fill:" + TEXT + ";" +
            "-fx-prompt-text-fill:" + MUTED + ";"
        );
        return f;
    }

    private static ComboBox<String> formCombo(String prompt, ObservableList<String> items) {
        ComboBox<String> cb = new ComboBox<>(items);
        cb.setPromptText(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(
            "-fx-background-color: rgba(255,255,255,0.74);" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:20;" +
            "-fx-background-radius:20;" +
            "-fx-font-size:13px;"
        );
        return cb;
    }

    private static Button goldButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(
            "-fx-background-color:" + GOLD + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:18;" +
            "-fx-border-radius:18;" +
            "-fx-padding:10 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(184,137,59,0.22), 12, 0.2, 0, 3);" +
            "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(GOLD + ";", GOLD_LIGHT + ";")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace(GOLD_LIGHT + ";", GOLD + ";")));
        return b;
    }

    private static Button greyButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(
            "-fx-background-color:#B2AC88;" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:18;" +
            "-fx-border-color: rgba(255,255,255,0.55);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:18;" +
            "-fx-padding:10 0;" +
            "-fx-cursor:hand;"
        );
        return b;
    }

    private static Button darkButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(
            "-fx-background-color:" + BTN_DARK + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:18;" +
            "-fx-border-radius:18;" +
            "-fx-padding:10 0;" +
            "-fx-cursor:hand;"
        );
        return b;
    }

    private static Button outlineButton(String text) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color:#E2A76F;" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:13px;" +
            "-fx-border-color: rgba(255,255,255,0.55);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:18;" +
            "-fx-background-radius:18;" +
            "-fx-padding:8 18;" +
            "-fx-cursor:hand;"
        );
        return b;
    }

    private static Label colLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + "; -fx-font-family:'Georgia';");
        return l;
    }

    private static VBox fieldBlock(String labelText, Node input, Label errLbl) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + "; -fx-font-family:'Georgia';");
        VBox box = new VBox(5, lbl, input, errLbl);
        box.setPadding(new Insets(0, 0, 10, 0));
        return box;
    }

    private static VBox vBox(String lbl, Node input) {
        Label l = new Label(lbl);
        l.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";");
        VBox b = new VBox(5, l, input);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private static Label msgLabel() {
        Label l = new Label("");
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-font-size:12px; -fx-padding:0 0 6 0;");
        return l;
    }

    private static Label errLabel() {
        Label l = new Label("");
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-font-size:11px; -fx-text-fill:" + ERROR + ";");
        return l;
    }

    private static Region spacer(double h) {
        Region r = new Region();
        r.setMinHeight(h);
        r.setPrefHeight(h);
        return r;
    }

    private static ScrollPane wrapScroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:transparent; -fx-background:transparent;");
        return sp;
    }

    private static VBox analyticsCard(String title, Node... nodes) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle(
            "-fx-background-color:" + CARD_BG + ";" +
            "-fx-background-radius:18;" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:18;" +
            "-fx-effect: dropshadow(gaussian, rgba(112,112,146,0.12), 14, 0.2, 0, 4);"
        );
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + DARK + "; -fx-font-family:'Georgia';");
        card.getChildren().add(lbl);
        for (Node n : nodes) card.getChildren().add(n);
        return card;
    }

    private static void clearErrs(Label... labels) {
        javafx.application.Platform.runLater(() -> { for (Label l : labels) l.setText(""); });
    }

    private static void showErr(Label l, String msg) {
        javafx.application.Platform.runLater(() -> {
            l.setStyle("-fx-font-size:11px; -fx-text-fill:" + ERROR + ";");
            l.setText(msg);
        });
    }

    private static void ok(Label l, String m) {
        l.setStyle("-fx-font-size:12px; -fx-text-fill:" + SUCCESS + ";");
        l.setText(m);
    }

    private static void err(Label l, String m) {
        l.setStyle("-fx-font-size:12px; -fx-text-fill:" + ERROR + ";");
        l.setText(m);
    }

    @SuppressWarnings("unchecked")
    private static <S, T> TableColumn<S, T> col(String header, String property, double width) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        c.setStyle("-fx-font-family:'Georgia'; -fx-font-size:12px;");
        return c;
    }
}