package views;

import controllers.BookingController;
import controllers.CustomerController;
import controllers.HallController;
import controllers.ServiceController;
import hall_wedding.Booking;
import hall_wedding.Customer;
import hall_wedding.Hall;
import hall_wedding.Hall_Wedding;
import hall_wedding.SceneManager;
import hall_wedding.Service;
import hall_wedding.Theme;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

public class BookingView {

    private static BorderPane mainRoot;
    private static VBox mainContentHost;
    private static VBox dashboardView;
    private static final double BUTTON_HEIGHT = 40.0;
    private static final double NAV_BUTTON_HEIGHT = 38.0;

    public static Scene build() {
        double width = Hall_Wedding.primaryStage != null ? Hall_Wedding.primaryStage.getWidth() : 1280;
        double height = Hall_Wedding.primaryStage != null ? Hall_Wedding.primaryStage.getHeight() : 900;

        mainRoot = new BorderPane();
        mainRoot.setStyle(Theme.pageBackgroundStyle());
        mainRoot.setTop(buildTopBar());
        dashboardView = buildDashboardView();
        mainContentHost = new VBox(dashboardView);
        mainContentHost.setAlignment(Pos.TOP_CENTER);
        mainContentHost.setFillWidth(true);
        mainRoot.setCenter(mainContentHost);
        return new Scene(mainRoot, width, height);
    }

    private static HBox buildTopBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 22, 14, 18));
        bar.setStyle(Theme.frostedPanelStyle() + "-fx-background-radius:0; -fx-border-radius:0; -fx-border-width:0 0 1 0;");

        Button back = navButton("← Back");
        back.setOnAction(e -> SceneManager.switchTo("admin"));

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 19px;");

        Label title = new Label("Booking Management");
        title.setStyle(Theme.titleStyle(20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label version = new Label("Wedding Hall System v1.0");
        version.setStyle(Theme.subtitleStyle());

        bar.getChildren().addAll(back, icon, title, spacer, version);
        return bar;
    }

    private static VBox buildDashboardView() {
        long totalBookings = BookingController.count();
        long pendingBookings = BookingController.getByStatus("PENDING").size();
        double totalRevenue = BookingController.getAll().stream().mapToDouble(Booking::getNetRevenueAmount).sum();

        VBox leftCard = statCard("Total Bookings", String.valueOf(totalBookings));
        VBox centerCard = statCard("Pending", String.valueOf(pendingBookings));
        VBox rightCard = statCard("Total Revenue", String.format(Locale.ENGLISH, "%.0f EGP", totalRevenue));
        // fix card widths to keep consistent appearance
        leftCard.setPrefWidth(260); leftCard.setMaxWidth(260);
        centerCard.setPrefWidth(260); centerCard.setMaxWidth(260);
        rightCard.setPrefWidth(260); rightCard.setMaxWidth(260);

        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox statsRow = new HBox(18, leftCard, spacer1, centerCard, spacer2, rightCard);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(18, 18, 8, 18));

        Button createBtn = goldButton("Create Booking");
        createBtn.setOnAction(e -> openCreateWindow());

        Button searchBtn = goldButton("Search Bookings");
        searchBtn.setOnAction(e -> openSearchWindow());

        Button updateBtn = goldButton("Update Booking");
        updateBtn.setOnAction(e -> openUpdateWindow());

        Button cancelBtn = dangerButton("Cancel Booking");
        cancelBtn.setOnAction(e -> openCancelWindow());

        Button analyticsBtn = roseButton("Analytics");
        analyticsBtn.setOnAction(e -> openAnalyticsWindow());

        HBox actions = new HBox(14, createBtn, searchBtn, updateBtn, cancelBtn, analyticsBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(0, 18, 18, 18));

        VBox content = new VBox(0, statsRow, actions);
        content.setMaxWidth(1120);
        content.setFillWidth(true);
        VBox.setVgrow(actions, Priority.NEVER);

        TableView<Booking> table = buildTable();
        reload(table);

        VBox tableWrap = new VBox(table);
        tableWrap.setPadding(new Insets(0, 18, 18, 18));
        tableWrap.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox body = new VBox(0, content, tableWrap);
        body.setStyle(Theme.pageBackgroundStyle());
        body.setFillWidth(true);
        body.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(content, Priority.NEVER);
        VBox.setVgrow(tableWrap, Priority.ALWAYS);
        return body;
    }

    private static void openCreateWindow() {
        showInlineContent("Create Booking", "➕", buildCreateContent());
    }

    private static void openSearchWindow() {
        showInlineContent("Search Bookings", "🔍", buildSearchContent());
    }

    private static void openUpdateWindow() {
        showInlineContent("Update Booking", "✏", buildUpdateContent());
    }

    private static void openCancelWindow() {
        showInlineContent("Cancel Booking", "🗑", buildCancelContent());
    }

    private static void openAnalyticsWindow() {
        showInlineContent("Analytics & Reports", "📊", buildAnalyticsContent());
    }

    private static void showInlineContent(String titleText, String iconText, Node content) {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 22, 14, 18));
        header.setStyle(Theme.frostedPanelStyle() + "-fx-background-radius:0; -fx-border-radius:0; -fx-border-width:0 0 1 0;");

        Label icon = new Label(iconText);
        icon.setStyle("-fx-font-size: 19px;");

        Label title = new Label(titleText);
        title.setStyle(Theme.titleStyle(20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button back = navButton("Back to Dashboard");
        back.setOnAction(e -> showDashboard());

        header.getChildren().setAll(icon, title, spacer, back);

        VBox contentWrap = new VBox(content);
        contentWrap.setAlignment(Pos.TOP_CENTER);
        contentWrap.setFillWidth(true);
        VBox.setVgrow(content, Priority.ALWAYS);
        if (content instanceof ScrollPane sp) {
            sp.setMaxWidth(1180);
        }

        mainContentHost.getChildren().setAll(header, contentWrap);
    }

    private static void showDashboard() {
        mainRoot.setTop(buildTopBar());
        // Rebuild dashboard to refresh stats and table
        dashboardView = buildDashboardView();
        mainContentHost.getChildren().setAll(dashboardView);
    }

    private static Node buildCreateContent() {
        ComboBox<Customer> customerBox = comboField("Select Customer", FXCollections.observableArrayList(CustomerController.getAll()));
        customerBox.setConverter(converter(c -> c == null ? "" : c.getName() + " - " + c.getSsn()));

        ComboBox<Hall> hallBox = comboField("Select Hall", FXCollections.observableArrayList(HallController.getAll()));
        hallBox.setConverter(converter(h -> h == null ? "" : h.getName()));

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Event Date");
        datePicker.setStyle(inputStyle());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        // Disable past dates and booked dates for selected hall
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(false);
                    setStyle("");
                    return;
                }
                // Disable past dates
                if (!item.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #CCCCCC; -fx-text-fill: #999999;");
                    return;
                }
                // Disable booked dates for selected hall
                Hall selectedHall = hallBox.getValue();
                if (selectedHall != null && selectedHall.getId() != null) {
                    List<Booking> bookings = BookingController.getByDateRangeAndHall(item, item, selectedHall);
                    for (Booking b : bookings) {
                        if (!"CANCELED".equals(b.getStatus())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: #FFFFFF;");
                            return;
                        }
                    }
                }
                setDisable(false);
                setStyle("");
            }
        });
        // Update date picker when hall changes
        hallBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            datePicker.setValue(null); // Reset date when hall changes
            if (datePicker.getDayCellFactory() != null) {
                datePicker.setDayCellFactory(datePicker.getDayCellFactory()); // Refresh
            }
        });

        TextField durationField = inputField("Duration (1-24 hours)");
        ComboBox<String> paymentBox = comboField("Payment Method", FXCollections.observableArrayList(
            "Cash", "Vodafone Cash", "InstaPay", "Bank Transfer"));
        CheckComboBox<Service> serviceList = serviceCheckComboBox();
        TextField amountField = inputField("Paid amount");
        Label priceInfoLabel = new Label("Total: - EGP | Minimum: - EGP");
        priceInfoLabel.setStyle("-fx-text-fill: #8B5A3C; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label message = msgLabel();

        Runnable suggest = () -> suggestCreateAmount(hallBox, durationField, serviceList, amountField, priceInfoLabel);
        hallBox.valueProperty().addListener((obs, oldValue, newValue) -> suggest.run());
        durationField.textProperty().addListener((obs, oldValue, newValue) -> suggest.run());
        serviceList.getCheckModel().getCheckedItems().addListener((javafx.collections.ListChangeListener<Service>) c -> {
            suggest.run();
            // Update paid amount to include service prices
            try {
                Hall hall = hallBox.getValue();
                if (hall == null) return;
                String durationText = durationField.getText().trim();
                if (durationText.isEmpty()) return;
                int duration = Integer.parseInt(durationText);
                if (duration <= 0) return;
                double total = hall.getPricePerHour() * duration;
                for (Service service : serviceList.getCheckModel().getCheckedItems()) {
                    total += service.getPrice();
                }
                double minDeposit = Math.ceil(total * 0.20);
                amountField.setText(String.format(Locale.ENGLISH, "%.0f", minDeposit));
            } catch (Exception ignored) {
            }
        });

        Button createBtn = goldButton("Create Booking");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> {
            clearMsg(message);
            try {
                Customer customer = requireValue(customerBox, "Customer");
                Hall hall = requireValue(hallBox, "Hall");
                LocalDate date = datePicker.getValue();
                if (date == null) throw new IllegalArgumentException("Event date is required");
                int duration = parseInt(durationField.getText().trim(), "Duration");
                if (duration < 1 || duration > 24) throw new IllegalArgumentException("Duration must be between 1 and 24 hours");
                String paymentMethod = paymentBox.getValue();
                if (paymentMethod == null || paymentMethod.isBlank()) throw new IllegalArgumentException("Payment method is required");
                double amount = parseDouble(amountField.getText().trim(), "Paid amount");
                List<Service> services = new ArrayList<>(serviceList.getCheckModel().getCheckedItems());
                double total = BookingController.create(customer, hall, date, duration, services, amount, paymentMethod);
                ok(message, "Booking created. Total: " + String.format(Locale.ENGLISH, "%.0f EGP", total));
                // Refresh dashboard after successful booking
                showDashboard();
            } catch (Exception ex) {
                err(message, ex.getMessage());
            }
        });

        VBox card = formCard("Booking Details",
            fieldBlock("CUSTOMER", customerBox, errLabel()),
            fieldBlock("HALL", hallBox, errLabel()),
            fieldBlock("EVENT DATE", datePicker, errLabel()),
            fieldBlock("DURATION", durationField, errLabel()),
            fieldBlock("PAYMENT METHOD", paymentBox, errLabel()),
            fieldBlock("SERVICES", serviceList, errLabel()),
            priceInfoLabel,
            fieldBlock("PAID AMOUNT", amountField, errLabel()),
            createBtn,
            message
        );

        return scrollWrap(card);
    }

    private static Node buildSearchContent() {
        DatePicker fromDate = new DatePicker();
        fromDate.setStyle(inputStyle());
        DatePicker toDate = new DatePicker();
        toDate.setStyle(inputStyle());

        ComboBox<Hall> hallBox = comboField("All Halls", FXCollections.observableArrayList(HallController.getAll()));
        hallBox.setConverter(converter(h -> h == null ? "All Halls" : h.getName()));
        ComboBox<Customer> customerBox = comboField("All Customers", FXCollections.observableArrayList(CustomerController.getAll()));
        customerBox.setConverter(converter(c -> c == null ? "All Customers" : c.getName() + " - " + c.getSsn()));
        ComboBox<String> statusBox = comboField("All Status", FXCollections.observableArrayList("PENDING", "CONFIRMED", "CANCELED"));

        Label message = msgLabel();
        TableView<Booking> table = buildTable();
        reload(table);

        Button dateSearch = goldButton("Search Date Range");
        dateSearch.setOnAction(e -> {
            clearMsg(message);
            try {
                List<Booking> result = BookingController.getByDateRangeAndHall(fromDate.getValue(), toDate.getValue(), hallBox.getValue());
                table.setItems(FXCollections.observableArrayList(result));
                ok(message, "Found " + result.size() + " booking(s)");
            } catch (Exception ex) {
                err(message, ex.getMessage());
            }
        });

        Button byCustomer = goldButton("By Customer");
        byCustomer.setOnAction(e -> {
            clearMsg(message);
            Customer customer = customerBox.getValue();
            if (customer == null) {
                err(message, "Select a customer");
                return;
            }
            List<Booking> result = BookingController.getByCustomer(customer.getId());
            table.setItems(FXCollections.observableArrayList(result));
            ok(message, "Found " + result.size() + " booking(s)");
        });

        Button byStatus = roseButton("By Status");
        byStatus.setOnAction(e -> {
            clearMsg(message);
            String status = statusBox.getValue();
            if (status == null) {
                err(message, "Select a status");
                return;
            }
            List<Booking> result = BookingController.getByStatus(status);
            table.setItems(FXCollections.observableArrayList(result));
            ok(message, "Found " + result.size() + " booking(s)");
        });

        Button reset = roseButton("Reset");
        reset.setOnAction(e -> {
            fromDate.setValue(null);
            toDate.setValue(null);
            hallBox.setValue(null);
            customerBox.setValue(null);
            statusBox.setValue(null);
            clearMsg(message);
            reload(table);
        });

        VBox card = formCard("Search Filters",
            row(fieldBlock("FROM", fromDate, errLabel()), fieldBlock("TO", toDate, errLabel()), fieldBlock("HALL", hallBox, errLabel())),
            row(dateSearch, reset),
            new Separator(),
            row(fieldBlock("CUSTOMER", customerBox, errLabel()), fieldBlock("STATUS", statusBox, errLabel())),
            row(byCustomer, byStatus),
            message
        );

        VBox body = new VBox(14, card, table);
        body.setPadding(new Insets(18));
        body.setStyle(Theme.pageBackgroundStyle());
        VBox.setVgrow(table, Priority.ALWAYS);
        return body;
    }

    private static Node buildUpdateContent() {
        TableView<Booking> table = buildTable();
        reload(table);

        ComboBox<Customer> customerBox = comboField("Select Customer", FXCollections.observableArrayList(CustomerController.getAll()));
        customerBox.setConverter(converter(c -> c == null ? "" : c.getName() + " - " + c.getSsn()));
        ComboBox<Hall> hallBox = comboField("Select Hall", FXCollections.observableArrayList(HallController.getAll()));
        hallBox.setConverter(converter(h -> h == null ? "" : h.getName()));
        DatePicker datePicker = new DatePicker();
        datePicker.setStyle(inputStyle());
        TextField durationField = inputField("Duration");
        ComboBox<String> paymentBox = comboField("Payment Method", FXCollections.observableArrayList(
            "Cash", "Vodafone Cash", "InstaPay", "Bank Transfer"));
        TextField amountField = inputField("Additional amount");
        CheckComboBox<Service> serviceList = serviceCheckComboBox();
        Label summary = new Label("Select a booking from the table");
        summary.setStyle(Theme.subtitleStyle());
        Label message = msgLabel();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
            if (value == null) {
                summary.setText("Select a booking from the table");
                return;
            }
            summary.setText("Booking #" + value.getId() + " | " + value.getStatus());
            customerBox.setValue(findCustomerById(customerBox.getItems(), value.getCustomer()));
            hallBox.setValue(findHallById(hallBox.getItems(), value.getHall()));
            datePicker.setValue(value.getEventDate());
            durationField.setText(String.valueOf(value.getDurationHours()));
            // Do not pre-fill payment method when selecting a booking.
            // Pre-filling caused accidental full-payment on update because
            // BookingController treated any non-empty paymentMethod as "immediate full payment".
            paymentBox.setValue(null);
            amountField.clear();
            serviceList.getCheckModel().clearChecks();
            for (Service service : value.getServices()) {
                Service matched = findServiceById(serviceList.getItems(), service);
                if (matched != null) {
                    serviceList.getCheckModel().check(matched);
                }
            }
        });

        Button update = goldButton("Update Selected");
        update.setOnAction(e -> {
            clearMsg(message);
            Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                err(message, "Select a booking first");
                return;
            }
            try {
                Customer customer = requireValue(customerBox, "Customer");
                Hall hall = requireValue(hallBox, "Hall");
                LocalDate date = datePicker.getValue();
                if (date == null) throw new IllegalArgumentException("Event date is required");
                int duration = parseInt(durationField.getText().trim(), "Duration");
                List<Service> services = new ArrayList<>(serviceList.getCheckModel().getCheckedItems());
                String paymentMethod = paymentBox.getValue();
                double extraAmount = amountField.getText().trim().isEmpty() ? 0.0 : parseDouble(amountField.getText().trim(), "Additional amount");
                double total = BookingController.update(selected.getId(), customer, hall, date, duration, services, extraAmount, paymentMethod);
                // If no payment method and no extra amount provided, it's a pure update (date/details only).
                if ((paymentMethod == null || paymentMethod.isBlank()) && extraAmount == 0.0) {
                    ok(message, "Updated.");
                } else {
                    ok(message, "Updated. Total: " + String.format(Locale.ENGLISH, "%.0f EGP", total));
                }
                reload(table);
            } catch (Exception ex) {
                err(message, ex.getMessage());
            }
        });

        Button payRemaining = roseButton("Pay Remaining");
        payRemaining.setOnAction(e -> {
            clearMsg(message);
            Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                err(message, "Select a booking first");
                return;
            }
            if (selected.isPaid()) {
                err(message, "Booking is already fully paid");
                return;
            }
            try {
                String paymentMethod = paymentBox.getValue();
                if (paymentMethod == null || paymentMethod.isBlank()) {
                    err(message, "Select a payment method");
                    return;
                }
                boolean done = BookingController.pay(selected.getId(), paymentMethod);
                if (done) {
                    ok(message, "Payment complete. Booking status changed to CONFIRMED");
                    reload(table);
                } else {
                    err(message, "Payment failed");
                }
            } catch (Exception ex) {
                err(message, ex.getMessage());
            }
        });

        VBox form = formCard("Edit Booking",
            summary,
            fieldBlock("CUSTOMER", customerBox, errLabel()),
            fieldBlock("HALL", hallBox, errLabel()),
            fieldBlock("EVENT DATE", datePicker, errLabel()),
            fieldBlock("DURATION", durationField, errLabel()),
            fieldBlock("PAYMENT METHOD", paymentBox, errLabel()),
            fieldBlock("ADDITIONAL AMOUNT", amountField, errLabel()),
            fieldBlock("SERVICES", serviceList, errLabel()),
            row(update, payRemaining),
            message
        );

        HBox content = new HBox(14, table, scrollWrap(form));
        content.setPadding(new Insets(18));
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);
        return content;
    }

    private static Node buildCancelContent() {
        TableView<Booking> table = buildTable();
        reload(table);
        Label message = msgLabel();

        Button cancel = dangerButton("Cancel Selected Booking");
        cancel.setOnAction(e -> {
            Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                err(message, "Select a booking first");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Booking");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("Cancel booking #" + selected.getId() + "?");
            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(yes, no);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == yes) {
                BookingController.cancel(selected.getId());
                err(message, "Booking canceled");
                reload(table);
            }
        });

        VBox card = formCard("Cancel Bookings",
            new Label("Select a booking from the table and cancel it."),
            cancel,
            message
        );

        HBox content = new HBox(14, table, scrollWrap(card));
        content.setPadding(new Insets(18));
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);
        return content;
    }

    private static Node buildAnalyticsContent() {
        ComboBox<Integer> monthBox = comboField("Month", FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        monthBox.setConverter(converter(m -> m == null ? "" : Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH)));
        ComboBox<Integer> yearBox = comboField("Year", FXCollections.observableArrayList(2023, 2024, 2025, 2026, 2027));
        yearBox.setValue(LocalDate.now().getYear());

        Label revenueLabel = new Label("—");
        revenueLabel.setStyle(Theme.titleStyle(15));
        Button revenueBtn = goldButton("Calculate Revenue");
        revenueBtn.setOnAction(e -> {
            Integer month = monthBox.getValue();
            Integer year = yearBox.getValue();
            if (month == null || year == null) {
                err(revenueLabel, "Select month and year");
                return;
            }
            double total = BookingController.getTotalRevenueByMonth(month, year);
            revenueLabel.setText(String.format(Locale.ENGLISH, "%s %d: %.0f EGP",
                Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), year, total));
        });

        ComboBox<Customer> customerBox = comboField("Customer", FXCollections.observableArrayList(CustomerController.getAll()));
        customerBox.setConverter(converter(c -> c == null ? "" : c.getName()));
        Label customerCountLabel = new Label("—");
        customerCountLabel.setStyle(Theme.titleStyle(15));
        Button customerBtn = roseButton("Count Bookings");
        customerBtn.setOnAction(e -> {
            Customer customer = customerBox.getValue();
            if (customer == null) {
                err(customerCountLabel, "Select a customer");
                return;
            }
            long count = BookingController.countByCustomer(customer.getId());
            customerCountLabel.setText(customer.getName() + ": " + count + " booking(s)");
        });

        VBox card = formCard("Analytics",
            fieldBlock("MONTH", monthBox, errLabel()),
            fieldBlock("YEAR", yearBox, errLabel()),
            revenueBtn,
            revenueLabel,
            new Separator(),
            fieldBlock("CUSTOMER", customerBox, errLabel()),
            customerBtn,
            customerCountLabel
        );

        return scrollWrap(card);
    }

    private static TableView<Booking> buildTable() {
        TableView<Booking> table = new TableView<>();
        table.setStyle(Theme.tableStyle());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Booking, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Booking, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getCustomerDisplayName() +
            (d.getValue().getCustomer() == null || d.getValue().getCustomer().getSsn() == null ? "" : " - " + d.getValue().getCustomer().getSsn())));

        TableColumn<Booking, String> hallCol = new TableColumn<>("Hall");
        hallCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getHall() == null ? "" : d.getValue().getHall().getName()));

        TableColumn<Booking, LocalDate> dateCol = new TableColumn<>("Event Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("eventDate"));

        TableColumn<Booking, Integer> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationHours"));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Booking, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        TableColumn<Booking, Double> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getGrossPaidAmount()).asObject());
        paidCol.setCellFactory(tc -> moneyCell());

        TableColumn<Booking, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalPrice()).asObject());
        totalCol.setCellFactory(tc -> moneyCell());

        TableColumn<Booking, Double> refundedCol = new TableColumn<>("Refunded Amount");
        refundedCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getRefundedAmount()).asObject());
        refundedCol.setCellFactory(tc -> moneyCell());

        table.getColumns().addAll(idCol, customerCol, hallCol, dateCol, durationCol, statusCol, paymentCol, paidCol, totalCol, refundedCol);
        table.setPlaceholder(new Label("No bookings found"));
        table.setRowFactory(tv -> new TableRow<Booking>() {
            @Override
            protected void updateItem(Booking item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle("-fx-background-color: " + Theme.GOLD + "; -fx-text-fill: " + Theme.CREAM + "; -fx-font-weight: 600;");
                } else {
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (table.getItems() != null) {
                for (int i = 0; i < table.getItems().size(); i++) {
                    table.refresh();
                    break;
                }
            }
        });
        return table;
    }

    private static TableCell<Booking, Double> moneyCell() {
        return new TableCell<Booking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.ENGLISH, "%.0f", item));
                    setStyle("-fx-font-weight:700; -fx-text-fill:" + Theme.CREAM + ";");
                }
            }
        };
    }

    private static VBox formCard(String titleText, Node... nodes) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(Theme.cardStyle());
        Label title = new Label(titleText);
        title.setStyle(Theme.titleStyle(16));
        card.getChildren().add(title);
        for (Node node : nodes) {
            card.getChildren().add(node);
        }
        return card;
    }

    private static VBox fieldBlock(String labelText, Node input, Label errLbl) {
        Label label = new Label(labelText);
        label.setStyle(Theme.subtitleStyle() + "-fx-font-weight:bold;");
        VBox box = new VBox(6, label, input, errLbl);
        box.setFillWidth(true);
        VBox.setVgrow(input, Priority.NEVER);
        return box;
    }

    private static VBox row(Node... nodes) {
        HBox row = new HBox(10, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        for (Node node : nodes) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        VBox wrapper = new VBox(row);
        wrapper.setFillWidth(true);
        return wrapper;
    }

    private static ScrollPane scrollWrap(Node node) {
        VBox box = new VBox(node);
        box.setPadding(new Insets(0));
        box.setStyle(Theme.pageBackgroundStyle());
        VBox.setVgrow(node, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setPannable(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return sp;
    }

    private static VBox statCard(String titleText, String valueText) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(Theme.frostedPanelStyle() + "-fx-background-radius:18; -fx-border-radius:18;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.CREAM + "; -fx-font-weight: 600;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: " + Theme.GOLD + ";");

        card.getChildren().addAll(title, value);
        return card;
    }

    private static CheckComboBox<Service> serviceCheckComboBox() {
        CheckComboBox<Service> comboBox = new CheckComboBox<>(FXCollections.observableArrayList(ServiceController.getAll()));
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setStyle(inputStyle());
        comboBox.setConverter(converter(s -> s == null ? "" : s.getName() + " - " + String.format(Locale.ENGLISH, "%.0f", s.getPrice()) + " EGP"));
        comboBox.setTitle("Select Services");
        comboBox.setPrefHeight(40);
        return comboBox;
    }

    private static void suggestCreateAmount(ComboBox<Hall> hallBox, TextField durationField, CheckComboBox<Service> serviceList, TextField amountField, Label priceInfoLabel) {
        try {
            Hall hall = hallBox.getValue();
            if (hall == null) {
                priceInfoLabel.setText("Total: - EGP | Minimum: - EGP");
                return;
            }
            String durationText = durationField.getText().trim();
            if (durationText.isEmpty()) {
                priceInfoLabel.setText("Total: - EGP | Minimum: - EGP");
                return;
            }
            int duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                priceInfoLabel.setText("Total: - EGP | Minimum: - EGP");
                return;
            }
            double total = hall.getPricePerHour() * duration;
            for (Service service : serviceList.getCheckModel().getCheckedItems()) {
                total += service.getPrice();
            }
            double minDeposit = Math.ceil(total * 0.20);
            priceInfoLabel.setText(String.format(Locale.ENGLISH, "Total: %.0f EGP | Minimum (20%%): %.0f EGP", total, minDeposit));
            // Always update the suggested amount
            amountField.setText(String.format(Locale.ENGLISH, "%.0f", minDeposit));
        } catch (Exception ignored) {
            priceInfoLabel.setText("Total: - EGP | Minimum: - EGP");
        }
    }

    private static Button goldButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(BUTTON_HEIGHT);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setAlignment(Pos.CENTER);
        button.setStyle(
            "-fx-background-color:" + Theme.GOLD + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:14px;" +
            "-fx-padding:10 22;" +
            "-fx-border-color: rgba(47,36,27,0.30);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:18;" +
            "-fx-border-radius:18;" +
            "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.18), 12, 0.2, 0, 3);" +
            "-fx-cursor:hand;"
        );
        return button;
    }

    private static Button roseButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(BUTTON_HEIGHT);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setAlignment(Pos.CENTER);
        button.setStyle(
            "-fx-background-color:" + Theme.ROSE + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:14px;" +
            "-fx-padding:10 22;" +
            "-fx-border-color: rgba(47,36,27,0.25);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:18;" +
            "-fx-border-radius:18;" +
            "-fx-cursor:hand;"
        );
        return button;
    }

    private static Button dangerButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(BUTTON_HEIGHT);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setAlignment(Pos.CENTER);
        button.setStyle(
            "-fx-background-color:#8B2020;" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:14px;" +
            "-fx-padding:10 22;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:18;" +
            "-fx-border-radius:18;" +
            "-fx-cursor:hand;"
        );
        return button;
    }

    private static Button navButton(String text) {
        Button button = new Button(text);
        button.setMinHeight(NAV_BUTTON_HEIGHT);
        button.setPrefHeight(NAV_BUTTON_HEIGHT);
        button.setPadding(new Insets(0, 16, 0, 16));
        button.setStyle(
            "-fx-background-color:" + Theme.GOLD + ";" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-weight:bold;" +
            "-fx-font-size:13px;" +
            "-fx-border-color: rgba(47,36,27,0.30);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:16;" +
            "-fx-background-radius:16;" +
            "-fx-effect: dropshadow(gaussian, rgba(86,57,22,0.18), 10, 0.2, 0, 2);" +
            "-fx-cursor:hand;"
        );
        return button;
    }

    private static Button outlineButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(BUTTON_HEIGHT);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setAlignment(Pos.CENTER);
        button.setStyle(
            "-fx-background-color:#E2A76F;" +
            "-fx-text-fill:#FFFFFF;" +
            "-fx-font-size:14px;" +
            "-fx-font-weight:bold;" +
            "-fx-border-color: rgba(255,255,255,0.55);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:18;" +
            "-fx-background-radius:18;" +
            "-fx-padding:10 22;" +
            "-fx-cursor:hand;"
        );
        return button;
    }

    private static TextField inputField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(inputStyle());
        return field;
    }

    private static <T> ComboBox<T> comboField(String prompt, ObservableList<T> items) {
        ComboBox<T> combo = new ComboBox<>(items);
        combo.setPromptText(prompt);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(inputStyle());
        return combo;
    }

    private static String inputStyle() {
        return Theme.inputStyle();
    }

    private static Label msgLabel() {
        Label label = new Label("");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.CREAM + ";");
        return label;
    }

    private static Label errLabel() {
        Label label = new Label("");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.ERROR + ";");
        return label;
    }

    private static void clearMsg(Label label) {
        label.setText("");
    }

    private static void ok(Label label, String msg) {
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.SUCCESS + ";");
        label.setText(msg);
    }

    private static void err(Label label, String msg) {
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.ERROR + ";");
        label.setText(msg);
    }

    private static void reload(TableView<Booking> table) {
        table.setItems(FXCollections.observableArrayList(BookingController.getAll()));
    }

    private static <T> T requireValue(ComboBox<T> box, String label) {
        T value = box.getValue();
        if (value == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }

    private static int parseInt(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return Integer.parseInt(value.trim());
    }

    private static double parseDouble(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return Double.parseDouble(value.trim());
    }

    private static Customer findCustomerById(List<Customer> list, Customer target) {
        if (target == null || target.getId() == null) return null;
        for (Customer item : list) {
            if (item != null && item.getId() != null && item.getId().equals(target.getId())) return item;
        }
        return null;
    }

    private static Hall findHallById(List<Hall> list, Hall target) {
        if (target == null || target.getId() == null) return null;
        for (Hall item : list) {
            if (item != null && item.getId() != null && item.getId().equals(target.getId())) return item;
        }
        return null;
    }

    private static Service findServiceById(List<Service> list, Service target) {
        if (target == null || target.getId() == null) return null;
        for (Service item : list) {
            if (item != null && item.getId() != null && item.getId().equals(target.getId())) return item;
        }
        return null;
    }

    private static <T> StringConverter<T> converter(Function<T, String> fn) {
        return new StringConverter<T>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : fn.apply(value);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }
}
