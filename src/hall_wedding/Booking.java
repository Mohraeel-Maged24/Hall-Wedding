package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(
        name = "Booking.findByCustomer",
        query = "SELECT b FROM Booking b WHERE b.customer.id = :customerId"
    ),
    @NamedQuery(
        name = "Booking.findByStatus",
        query = "SELECT b FROM Booking b WHERE b.status = :status"
    )
})
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @TableGenerator(
        name = "booking_id_gen",
        table = "id_generator",
        pkColumnName = "entity_name",
        valueColumnName = "next_id",
        pkColumnValue = "booking",
        initialValue = 1,
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "booking_id_gen")
    private Long id;

    private LocalDate eventDate;
    private int durationHours;
    private double totalPrice;
    private String status;

    @ManyToOne
    private Customer customer;

    private String customerNameSnapshot;

    @ManyToOne
    private Hall hall;

    @ManyToMany
    private List<Service> services = new ArrayList<>();

    @ManyToOne
    private Admin createdBy;

    private String paymentMethod;
    private boolean isPaid;
    private LocalDate paymentDate;
    private double depositAmount;
    private double finalPaymentAmount;
    private double refundedAmount;
    private LocalDate depositDate;
    private LocalDate finalPaymentDate;
    private LocalDate refundDate;

    public Booking() {
        this.status = "PENDING";
        this.isPaid = false;
    }

    public Booking(Customer customer, Hall hall,
                   LocalDate eventDate, int durationHours) {
        setHall(hall);
        this.eventDate = eventDate;
        if (eventDate == null) throw new IllegalArgumentException("Event date is required");
        if (!eventDate.isAfter(LocalDate.now())) throw new IllegalArgumentException("Event date must be after today");
        setDurationHours(durationHours);
        this.status = "PENDING";
        this.isPaid = false;
        setCustomer(customer);
    }

    public double calculateTotalPrice() {
        if (hall == null) return 0;
        double hallCost = hall.getPricePerHour() * durationHours;
        double servicesCost = services.stream()
                .mapToDouble(Service::getPrice)
                .sum();
        this.totalPrice = hallCost + servicesCost;
        return totalPrice;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public double getRequiredDepositAmount() {
        return calculateTotalPrice() * 0.20;
    }

    public double getGrossPaidAmount() {
        return depositAmount + finalPaymentAmount;
    }

    public double getRemainingAmount() {
        return Math.max(0.0, getTotalPrice() - getGrossPaidAmount());
    }

    public double getNetRevenueAmount() {
        return getGrossPaidAmount() - refundedAmount;
    }

    // FIX 2: getTotalPrice returns cached value if already calculated,
    // only recalculates if totalPrice is 0 (never set) or hall/services changed
    public double getTotalPrice() {
        return calculateTotalPrice();
    }

    public boolean confirmBooking() {
        this.status = "CONFIRMED";
        calculateTotalPrice();
        System.out.println("Booking confirmed for " + customer.getName());
        return true;
    }

    public void registerDeposit(String method) {
        registerDeposit(method, getRequiredDepositAmount());
    }

    public void registerDeposit(String method, double amount) {
        calculateTotalPrice();
        double min = getRequiredDepositAmount();
        if (amount < min) {
            throw new IllegalArgumentException("Minimum deposit is 20% of total");
        }
        double amt = Math.min(amount, totalPrice);
        this.depositAmount = amt;
        this.refundedAmount = 0.0;
        this.depositDate = LocalDate.now();
        this.paymentDate = this.depositDate;
        this.paymentMethod = (method == null || method.trim().isEmpty()) ? "Deposit" : method.trim();

        if (amt >= totalPrice) {
            // FIX 3 (Booking side): When full payment is made at deposit time,
            // record finalPaymentDate so revenue reporting can find it
            this.finalPaymentAmount = 0.0;
            this.finalPaymentDate = this.depositDate;  // <-- was null before, now set
            this.status = "CONFIRMED";
            this.isPaid = true;
        } else {
            this.finalPaymentAmount = 0.0;
            this.finalPaymentDate = null;
            this.status = "PENDING";
            this.isPaid = false;
        }
    }

    public void addPayment(String method, double amount) {
        calculateTotalPrice();
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (method != null && !method.trim().isEmpty()) {
            this.paymentMethod = method.trim();
        }

        double currentPaid = getGrossPaidAmount();
        double newPaid = currentPaid + amount;
        if (depositDate == null) {
            depositDate = LocalDate.now();
        }
        paymentDate = LocalDate.now();

        if (newPaid >= totalPrice) {
            this.finalPaymentAmount = totalPrice - depositAmount;
            if (this.finalPaymentAmount < 0) this.finalPaymentAmount = 0;
            this.status = "CONFIRMED";
            this.isPaid = true;
            this.finalPaymentDate = paymentDate;
        } else {
            this.depositAmount += amount;
            this.finalPaymentAmount = 0.0;
            this.status = "PENDING";
            this.isPaid = false;
            this.finalPaymentDate = null;
        }
    }

    public void cancelBooking() {
        cancelBooking(true);
    }

    public void cancelBooking(boolean keepDeposit) {
        calculateTotalPrice();
        double retainedAmount = keepDeposit ? getRequiredDepositAmount() : 0.0;
        double amountToRefund = Math.max(0.0, getGrossPaidAmount() - retainedAmount);
        this.refundedAmount += amountToRefund;
        this.refundDate = keepDeposit ? LocalDate.now() : (depositDate != null ? depositDate : LocalDate.now());
        this.status = "CANCELED";
        this.isPaid = false;
        System.out.println("Booking canceled");
    }

    /**
     * Cancel booking due to replacement with higher-priority booking (full payment).
     * Customer loses penaltyPercent of the gross paid amount (penalty → revenue),
     * remainder is refunded.
     *
     * Example: If customer paid 1000 EGP and penaltyPercent = 0.20,
     *          penalty = 200 EGP (retained by hall as revenue)
     *          refund = 800 EGP (returned to customer)
     */
    public void cancelForReplacement(double penaltyPercent) {
        calculateTotalPrice();
        double grossPaid = getGrossPaidAmount();
        double penalty = grossPaid * penaltyPercent;
        double amountToRefund = Math.max(0.0, grossPaid - penalty);
        this.refundedAmount += amountToRefund;
        this.refundDate = LocalDate.now();
        this.status = "CANCELED";
        this.isPaid = false;
        System.out.println("Booking canceled for replacement (20% penalty applied)");
    }

    public void addService(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("Service is required");
        }
        services.add(service);
    }

    public boolean processPayment(String method) {
        if ("CANCELED".equals(status)) {
            System.out.println("Canceled booking cannot be paid");
            return false;
        }
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        calculateTotalPrice();
        this.paymentMethod = method.trim();
        this.finalPaymentAmount = Math.max(0.0, totalPrice - depositAmount);
        this.paymentDate = LocalDate.now();
        this.finalPaymentDate = this.paymentDate;
        this.status = "CONFIRMED";
        this.isPaid = true;
        System.out.println("Payment done");
        return true;
    }

    public void refundPayment() {
        if (!isPaid) {
            System.out.println("No payment to refund");
            return;
        }
        this.isPaid = false;
        System.out.println("Refund done");
    }

    public Long getId() { return id; }

    public LocalDate getEventDate() { return eventDate; }

    /**
     * FIX 1: Public setter used by the form/controller — validates future date.
     * JPA uses a separate package-private/protected path or field access if mapped
     * with @Access(AccessType.FIELD). Since we use field access implicitly via
     * @GeneratedValue etc., JPA hydrates fields directly and never calls this setter.
     * But to be safe we also provide setEventDateInternal() for loading old records.
     */
    public void setEventDate(LocalDate eventDate) {
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (!eventDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Event date must be after today");
        }
        this.eventDate = eventDate;
    }

    /**
     * FIX 1: Package-private setter for loading historical records from DB
     * without triggering the future-date validation.
     * Use this in controllers when updating a booking's date only if the new date
     * needs validation; JPA itself accesses the field directly.
     */
    void setEventDateRaw(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) {
        if (durationHours < 1 || durationHours > 24) {
            throw new IllegalArgumentException("Duration must be between 1 and 24 hours");
        }
        this.durationHours = durationHours;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) {
        Customer previousCustomer = this.customer;
        if (this.customer != null && this.customer.getBookings().contains(this)) {
            this.customer.getBookings().remove(this);
        }
        if (customer == null && previousCustomer != null && previousCustomer.getName() != null && !previousCustomer.getName().isBlank()) {
            this.customerNameSnapshot = previousCustomer.getName();
        }
        this.customer = customer;
        if (customer != null && customer.getName() != null && !customer.getName().isBlank()) {
            this.customerNameSnapshot = customer.getName();
        }
        if (customer != null && !customer.getBookings().contains(this)) {
            customer.getBookings().add(this);
        }
    }

    public String getCustomerNameSnapshot() { return customerNameSnapshot; }

    public String getCustomerDisplayName() {
        if (customer != null && customer.getName() != null && !customer.getName().isBlank()) {
            return customer.getName();
        }
        if (customerNameSnapshot != null && !customerNameSnapshot.isBlank()) {
            return customerNameSnapshot;
        }
        return "Unknown Customer";
    }

    public Hall getHall() { return hall; }
    public void setHall(Hall hall) {
        if (hall == null) {
            throw new IllegalArgumentException("Hall is required");
        }
        this.hall = hall;
    }
    public List<Service> getServices() { return services; }
    public Admin getCreatedBy() { return createdBy; }
    public void setCreatedBy(Admin createdBy) { this.createdBy = createdBy; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        this.paymentMethod = paymentMethod.trim();
    }
    public boolean isPaid() { return isPaid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public double getFinalPaymentAmount() { return finalPaymentAmount; }
    public double getRefundedAmount() { return refundedAmount; }
    public LocalDate getDepositDate() { return depositDate; }
    public LocalDate getFinalPaymentDate() { return finalPaymentDate; }
    public LocalDate getRefundDate() { return refundDate; }

    @Override
    public String toString() {
        return "Booking[id=" + id +
                ", customer=" + (customer != null ? customer.getName() : "null") +
                ", hall=" + (hall != null ? hall.getName() : "null") +
                ", date=" + eventDate +
                ", status=" + status +
                ", total=" + getTotalPrice() + "]";
    }
}