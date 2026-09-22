package views;

import controllers.HallController;
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

public class HallView {

    private static VBox formNode;

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    // ── Colour tokens: premium glass + pastel accents ────────────────────
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

    private static final ObservableList<String> GOVERNORATES = FXCollections.observableArrayList(
        "Cairo", "Giza", "Alexandria", "Dakahlia", "Red Sea", "Beheira", "Fayoum",
        "Gharbia", "Ismailia", "Menofia", "Minya", "Qaliubiya", "New Valley",
        "Suez", "Aswan", "Assiut", "Beni Suef", "Port Said", "Damietta",
        "Sharkia", "South Sinai", "Kafr El Sheikh", "Matrouh", "Luxor", "Qena",
        "North Sinai", "Sohag"
    );

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

    private static Pane buildBackdrop() {
        Pane backdrop = new Pane();
        backdrop.setMouseTransparent(true);
        backdrop.setPickOnBounds(false);

        Circle c1 = floatingCircle(150, Color.web("#DCCCF3", 0.35), 40, 40);
        Circle c2 = floatingCircle(120, Color.web("#E8D8C0", 0.30), 760, 90);
        Circle c3 = floatingCircle(170, Color.web("#CFE4EC", 0.26), 200, 520);

        backdrop.getChildren().addAll(c1, c2, c3);
        drift(c1, -10, 12, 5200, 0);
        drift(c2, 12, -10, 6000, 140);
        drift(c3, 10, -8, 5600, 300);
        return backdrop;
    }

    private static Circle floatingCircle(double radius, Color fill, double x, double y) {
        Circle circle = new Circle(radius, fill);
        circle.setCenterX(x + radius);
        circle.setCenterY(y + radius);
        circle.setStroke(Color.web("rgba(255,255,255,0.55)"));
        circle.setStrokeWidth(1.2);
        return circle;
    }

    private static void drift(Circle circle, double dx, double dy, double durationMs, double delayMs) {
        TranslateTransition t = new TranslateTransition(Duration.millis(durationMs), circle);
        t.setByX(dx);
        t.setByY(dy);
        t.setAutoReverse(true);
        t.setCycleCount(Animation.INDEFINITE);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.setDelay(Duration.millis(delayMs));
        t.play();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TOP BAR  — matches screenshot: Back | 🏛 HALL MANAGEMENT | … v1.0
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

        // Hall icon + title
        Label icon = new Label("🏛");
        icon.setStyle("-fx-font-size:20px;");

        Label title = new Label("HALL MANAGEMENT");
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
    //  BODY  — left form panel + right tabs panel
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
    //  LEFT — Add / Edit Hall form
    // ═══════════════════════════════════════════════════════════════════════
    private static TableView<Hall> sharedTableRef;   // shared reference so form can refresh right-side table

    private static VBox buildFormPanel() {
        // ── Fields ──
        TextField nameF     = formField("Hall Name");
        ComboBox<String> locationF = formCombo("Location", GOVERNORATES);
        TextField capacityF = formField("Capacity");
        TextField priceF    = formField("Price Per Hour");

        Label msgLbl       = msgLabel();
        Label nameErr      = errLabel();
        Label locationErr  = errLabel();
        Label capacityErr  = errLabel();
        Label priceErr     = errLabel();

        // ── Buttons ──
        Button addBtn    = goldButton("＋  Add Hall");
        Button updateBtn = greyButton("↻  Update Hall");
        Button deleteBtn = darkButton("🗑  Delete Hall");

        // ── Add action ──
        addBtn.setOnAction(e -> {
            clearErrs(nameErr, locationErr, capacityErr, priceErr);
            msgLbl.setText("");
            String name       = nameF.getText().trim();
            String location   = locationF.getValue();
            String capTxt     = capacityF.getText().trim();
            String priceTxt   = priceF.getText().trim();
            boolean bad = false;
            if (name.isEmpty())                                     { showErr(nameErr, "Hall name is required"); bad = true; }
            else if (name.length() < 3)                             { showErr(nameErr, "Min 3 characters"); bad = true; }
            else if (!name.matches(NAME_REGEX))                     { showErr(nameErr, "Letters only"); bad = true; }
            if (location == null)                                   { showErr(locationErr, "Location is required"); bad = true; }
            if (capTxt.isEmpty())                                   { showErr(capacityErr, "Capacity is required"); bad = true; }
            if (priceTxt.isEmpty())                                 { showErr(priceErr, "Price is required"); bad = true; }
            Integer cap = null; Double price = null;
            if (!capTxt.isEmpty())   { try { cap   = Integer.parseInt(capTxt);    } catch (NumberFormatException ex) { showErr(capacityErr, "Must be a number"); bad = true; } }
            if (!priceTxt.isEmpty()) { try { price = Double.parseDouble(priceTxt);} catch (NumberFormatException ex) { showErr(priceErr,    "Must be a number"); bad = true; } }
            if (cap   != null && (cap < 10 || cap > 10000)) { showErr(capacityErr, "10–10000"); bad = true; }
            if (price != null && price < 5000)              { showErr(priceErr,    "Min 5000 EGP/hr"); bad = true; }
            if (bad) return;
            try {
                HallController.add(name, location, cap, price);
                ok(msgLbl, "✅ Hall added!");
                nameF.clear(); capacityF.clear(); priceF.clear(); locationF.setValue(null);
                reloadShared();
            } catch (Exception ex) { err(msgLbl, ex.getMessage()); }
        });

        // ── Update action ──
        updateBtn.setOnAction(e -> {
            if (sharedTableRef == null || sharedTableRef.getSelectionModel().getSelectedItem() == null) { err(msgLbl, "Select a hall first"); return; }
            Hall sel = sharedTableRef.getSelectionModel().getSelectedItem();
            clearErrs(nameErr, locationErr, capacityErr, priceErr);
            msgLbl.setText("");
            String name       = nameF.getText().trim();
            String location   = locationF.getValue();
            String capTxt     = capacityF.getText().trim();
            String priceTxt   = priceF.getText().trim();
            boolean bad = false;
            if (name.isEmpty())          { showErr(nameErr, "Hall name is required"); bad = true; }
            else if (name.length() < 3)  { showErr(nameErr, "Min 3 characters"); bad = true; }
            else if (!name.matches(NAME_REGEX)) { showErr(nameErr, "Letters only"); bad = true; }
            if (location == null)        { showErr(locationErr, "Location is required"); bad = true; }
            if (capTxt.isEmpty())        { showErr(capacityErr, "Capacity is required"); bad = true; }
            if (priceTxt.isEmpty())      { showErr(priceErr, "Price is required"); bad = true; }
            Integer cap = null; Double price = null;
            if (!capTxt.isEmpty())   { try { cap   = Integer.parseInt(capTxt);    } catch (NumberFormatException ex) { showErr(capacityErr, "Must be a number"); bad = true; } }
            if (!priceTxt.isEmpty()) { try { price = Double.parseDouble(priceTxt);} catch (NumberFormatException ex) { showErr(priceErr,    "Must be a number"); bad = true; } }
            if (cap   != null && (cap < 10 || cap > 10000)) { showErr(capacityErr, "10–10000"); bad = true; }
            if (price != null && price < 5000)              { showErr(priceErr,    "Min 5000 EGP/hr"); bad = true; }
            if (bad) return;
            try {
                HallController.update(sel.getId(), name, location, cap, price);
                ok(msgLbl, "✅ Updated!");
                reloadShared();
            } catch (Exception ex) { err(msgLbl, ex.getMessage()); }
        });

        // ── Delete action ──
        deleteBtn.setOnAction(e -> {
            if (sharedTableRef == null || sharedTableRef.getSelectionModel().getSelectedItem() == null) { err(msgLbl, "Select a hall first"); return; }
            Hall sel = sharedTableRef.getSelectionModel().getSelectedItem();
            try {
                HallController.delete(sel.getId());
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

        Label formTitle = new Label("✎  Add / Edit Hall");
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
            fieldBlock("HALL NAME",      nameF,      nameErr),
            fieldBlock("GOVERNORATE",    locationF,  locationErr),
            fieldBlock("CAPACITY",       capacityF,  capacityErr),
            fieldBlock("PRICE PER HOUR", priceF,     priceErr),
            spacer(16),
            addBtn,
            spacer(8),
            updateBtn,
            spacer(8),
            deleteBtn
        );

        // ── When table row selected, fill form ──
        // (wired later via sharedTableRef listener in buildRightPanel)

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        VBox wrapper = new VBox(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        wrapper.setStyle("-fx-background-color: transparent;");

        // store field refs on the form node so the selection listener can fill them
        form.getProperties().put("nameF",     nameF);
        form.getProperties().put("locationF", locationF);
        form.getProperties().put("capacityF", capacityF);
        form.getProperties().put("priceF",    priceF);

        return wrapper;
    }

    // ── fill form from selected hall (called after table is built) ─────────
    private static VBox formPanelRef;

    // ═══════════════════════════════════════════════════════════════════════
    //  RIGHT — TabPane: Search Hall | Analytics
    // ═══════════════════════════════════════════════════════════════════════
    private static VBox buildRightPanel() {
        // Build the shared table ONCE and keep reference
        sharedTableRef = buildTable();
        reloadTable(sharedTableRef);

        // Wire form fill on selection
        // (formPanelRef not available yet – we'll do this lazily)

        Tab searchTab    = new Tab("🔍  Search Hall", buildSearchContent(sharedTableRef));
        Tab analyticsTab = new Tab("📊  Analytics",   wrapScroll(buildAnalyticsContent()));
        searchTab.setClosable(false);
        analyticsTab.setClosable(false);

        TabPane tabs = new TabPane(searchTab, analyticsTab);
        tabs.setStyle(
            "-fx-background-color:" + TAB_BG + ";" +
            "-fx-tab-min-width:140px;" +
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

    // ── Search tab content ────────────────────────────────────────────────
    private static Node buildSearchContent(TableView<Hall> table) {
        TextField searchNameF  = formField("Search by name");
        ComboBox<String> searchLocF = formCombo("Filter by location", GOVERNORATES);
        Label searchMsg = msgLabel();

        Button searchBtn = goldButton("🔍  Search");
        Button resetBtn  = outlineButton("↺  Reset");
        searchBtn.setPrefWidth(150);
        resetBtn.setPrefWidth(150);

        searchBtn.setOnAction(e -> {
            try {
                String nm = searchNameF.getText().trim();
                String lc = searchLocF.getValue();
                List<Hall> res = HallController.searchByNameAndLocation(nm.isEmpty() ? null : nm, lc);
                table.setItems(FXCollections.observableArrayList(res));
                ok(searchMsg, "Found " + res.size() + " hall(s)");
            } catch (Exception ex) { err(searchMsg, ex.getMessage()); }
        });

        resetBtn.setOnAction(e -> {
            searchNameF.clear(); searchLocF.setValue(null); searchMsg.setText("");
            reloadTable(table);
        });

        // Fill form when row selected
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null && formNode != null) {
                javafx.application.Platform.runLater(() -> {
                    TextField nf = (TextField) formNode.getProperties().get("nameF");
                    ComboBox<String> lf = (ComboBox<String>) formNode.getProperties().get("locationF");
                    TextField cf = (TextField) formNode.getProperties().get("capacityF");
                    TextField pf = (TextField) formNode.getProperties().get("priceF");
                    if (nf != null) nf.setText(sel.getName());
                    if (lf != null) lf.setValue(sel.getLocation());
                    if (cf != null) cf.setText(String.valueOf(sel.getCapacity()));
                    if (pf != null) pf.setText(String.valueOf(sel.getPricePerHour()));
                });
            }
        });

        // Header row: NAME | LOCATION labels + inputs
        Label nameLabel = colLabel("NAME");
        Label locLabel  = colLabel("LOCATION");

        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        VBox nameBox = new VBox(6, nameLabel, searchNameF);
        VBox locBox  = new VBox(6, locLabel, searchLocF);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        HBox.setHgrow(locBox,  Priority.ALWAYS);
        filterRow.getChildren().addAll(nameBox, locBox);

        HBox btnRow = new HBox(14, searchBtn, resetBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        btnRow.setPadding(new Insets(4, 0, 8, 0));

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color:" + CARD_BG + "; -fx-background-radius:22; -fx-border-color:" + BORDER + "; -fx-border-width:1; -fx-border-radius:22;");
        content.setPadding(new Insets(20, 24, 20, 24));
        content.setSpacing(0);
        content.getChildren().addAll(filterRow, spacer(14), btnRow, searchMsg, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return content;
    }

    // ── Analytics tab content ─────────────────────────────────────────────
    private static VBox buildAnalyticsContent() {
        // Price range search
        TextField minPriceF = formField("e.g. 5000");
        TextField maxPriceF = formField("e.g. 10000");
        minPriceF.textProperty().addListener((o, ov, nv) -> { if (!nv.matches("\\d*")) minPriceF.setText(nv.replaceAll("[^\\d]", "")); });
        maxPriceF.textProperty().addListener((o, ov, nv) -> { if (!nv.matches("\\d*")) maxPriceF.setText(nv.replaceAll("[^\\d]", "")); });
        Label priceMsg = msgLabel();
        TableView<Hall> priceTable = buildTable();
        priceTable.setPrefHeight(170);
        Button priceSearchBtn = goldButton("Search");
        Button priceResetBtn  = outlineButton("Reset");
        priceSearchBtn.setOnAction(e -> {
            String mn = minPriceF.getText().trim(), mx = maxPriceF.getText().trim();
            if (mn.isEmpty() || mx.isEmpty()) { err(priceMsg, "Both values required"); return; }
            int minP, maxP;
            try { minP = Integer.parseInt(mn); maxP = Integer.parseInt(mx); }
            catch (NumberFormatException ex) { err(priceMsg, "Numbers only"); return; }
            if (minP > maxP) { err(priceMsg, "Min > Max"); return; }
            try {
                List<Hall> res = HallController.searchByPriceRange(minP, maxP);
                priceTable.setItems(FXCollections.observableArrayList(res));
                ok(priceMsg, "Found " + res.size() + " hall(s)");
            } catch (Exception ex) { err(priceMsg, ex.getMessage()); }
        });
        priceResetBtn.setOnAction(e -> { minPriceF.clear(); maxPriceF.clear(); priceMsg.setText(""); priceTable.getItems().clear(); });

        // Hall price and booking count
        ComboBox<Hall> statsCombo = hallCombo();
        Label statsLbl = new Label("—"); statsLbl.setStyle("-fx-font-size:13px; -fx-text-fill:" + GOLD + "; -fx-font-weight:bold;"); statsLbl.setWrapText(true);
        Button statsBtn = goldButton("Show Stats");
        statsBtn.setOnAction(e -> {
            Hall h = statsCombo.getValue();
            if (h == null) { statsLbl.setText("Select a hall first"); return; }
            long bookings = HallController.getBookingCountForHall(h.getId());
            statsLbl.setText(String.format("Price: %.0f EGP/hr  |  Bookings: %d", h.getPricePerHour(), bookings));
        });

        // Hall booking count
        ComboBox<Hall> countCombo = hallCombo();
        Label countLbl = new Label("—"); countLbl.setStyle("-fx-font-size:15px; -fx-text-fill:" + GOLD + "; -fx-font-weight:bold;");
        Button countBtn = outlineButton("Show Count");
        countBtn.setOnAction(e -> {
            Hall h = countCombo.getValue();
            if (h == null) { countLbl.setText("Select a hall first"); return; }
            countLbl.setText(h.getName() + ": " + HallController.getBookingCountForHall(h.getId()) + " booking(s)");
        });

        // Rankings
        Label rankLbl = new Label("—"); rankLbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + TEXT + ";"); rankLbl.setWrapText(true);
        Button rankBtn = goldButton("Rank All Halls");
        rankBtn.setOnAction(e -> {
            List<Object[]> rows = HallController.getAllHallsBookingCount();
            if (rows.isEmpty()) { rankLbl.setText("No data yet"); return; }
            StringBuilder sb = new StringBuilder();
            int r = 1;
            for (Object[] row : rows) {
                sb.append(r).append(". ").append(row[0]).append(" — ").append(row[1]).append(" booking(s)\n");
                r++;
            }
            rankLbl.setText(sb.toString().trim());
        });

        VBox analytics = new VBox(14);
        analytics.setPadding(new Insets(20, 24, 24, 24));
        analytics.getChildren().addAll(
            analyticsCard("💰 Price Range Search",
                new HBox(12, vBox("Min Price (EGP/hr)", minPriceF), vBox("Max Price (EGP/hr)", maxPriceF)),
                new HBox(8, priceSearchBtn, priceResetBtn),
                priceMsg, priceTable),
            analyticsCard("📊 Hall Price & Bookings", statsCombo, statsBtn, statsLbl),
            analyticsCard("🔢 Hall Total Bookings",  countCombo, countBtn, countLbl),
            analyticsCard("🏆 All Halls Ranking",    rankBtn, rankLbl)
        );
        return analytics;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SHARED TABLE
    // ═══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private static TableView<Hall> buildTable() {
        TableView<Hall> t = new TableView<>();
        t.setStyle(
            "-fx-background-color: rgba(255,255,255,0.40);" +
            "-fx-background-radius:16;" +
            "-fx-border-color:transparent;" +
            "-fx-font-size:13px;" +
            "-fx-font-family:'Georgia';"
        );
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Hall, Integer> idCol     = col("ID",       "id",           60);
        TableColumn<Hall, String>  nameCol   = col("NAME",     "name",         200);
        TableColumn<Hall, String>  locCol    = col("LOCATION", "location",     160);
        TableColumn<Hall, Integer> capCol    = col("CAPACITY", "capacity",     110);
        TableColumn<Hall, Double>  priceCol  = col("PRICE/HR", "pricePerHour", 130);

        // Style price column black
        priceCol.setCellFactory(tc -> new TableCell<Hall, Double>() {
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
            protected void updateItem(Hall item, boolean empty) {
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

        t.getColumns().addAll(idCol, nameCol, locCol, capCol, priceCol);
        t.setPlaceholder(new Label("No halls found"));
        return t;
    }

    private static void reloadTable(TableView<Hall> t) {
        try { t.setItems(FXCollections.observableArrayList(HallController.getAll())); }
        catch (Exception e) { t.setItems(FXCollections.observableArrayList()); }
    }

    private static void reloadShared() {
        if (sharedTableRef != null) reloadTable(sharedTableRef);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SMALL HELPERS
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

    private static ComboBox<Hall> hallCombo() {
        ObservableList<Hall> list = FXCollections.observableArrayList();
        try { list.addAll(HallController.getAll()); } catch (Exception ignored) {}
        ComboBox<Hall> cb = new ComboBox<>(list);
        cb.setPromptText("Select Hall");
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(formField("").getStyle());
        cb.setConverter(new javafx.util.StringConverter<Hall>() {
            public String toString(Hall h) { return h == null ? "" : h.getName(); }
            public Hall fromString(String s) { return null; }
        });
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
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(GOLD, GOLD_LIGHT)));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle().replace(GOLD_LIGHT, GOLD)));
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

    // Keep old topBar method for compatibility if used elsewhere
    static HBox topBar(String title, String backPage) {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 24, 12, 16));
        bar.setStyle("-fx-background-color:" + CARD_BG + "; -fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0;");
        Button back = new Button("← Back");
        back.setStyle("-fx-background-color:transparent; -fx-border-color:" + BORDER + "; -fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 14; -fx-font-size:13px; -fx-cursor:hand;");
        back.setOnAction(e -> SceneManager.switchTo(backPage));
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-font-family:'Georgia'; -fx-text-fill:" + DARK + ";");
        bar.getChildren().addAll(back, lbl);
        return bar;
    }
}