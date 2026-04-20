package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDate eventDate;
    private int durationHours;
    private double totalPrice;
    private String status;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Hall hall;

    @ManyToMany
    private List<Service> services = new ArrayList<>();

    @ManyToOne
    private Admin createdBy;

    private String paymentMethod;
    private boolean isPaid;
    private LocalDate paymentDate;

    public Booking() {
        this.status = "PENDING";
        this.isPaid = false;
    }

    public Booking(Customer customer, Hall hall,
                   LocalDate eventDate, int durationHours) {
        this.hall = hall;
        this.eventDate = eventDate;
        this.durationHours = durationHours;
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

    // FIX 2: getTotalPrice كان بيرجع القيمة المخزنة فقط (صفر لو مش محسوبة)
    // دلوقتي بيحسبها أول ما حد يطلبها
    public double getTotalPrice() {
        return calculateTotalPrice();
    }

    public boolean confirmBooking() {
        this.status = "CONFIRMED";
        calculateTotalPrice();
        System.out.println("Booking confirmed for " + customer.getName());
        return true;
    }

    public void cancelBooking() {
        this.status = "CANCELED";
        this.isPaid = false;
        System.out.println("Booking canceled");
    }

    public void addService(Service service) {
        services.add(service);
    }

    public boolean processPayment(String method) {
        if (!"CONFIRMED".equals(status)) {
            System.out.println("Booking not confirmed");
            return false;
        }

        calculateTotalPrice();
        this.paymentMethod = method;
        this.isPaid = true;
        this.paymentDate = LocalDate.now();

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
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) {
        if (this.customer != null && this.customer.getBookings().contains(this)) {
            this.customer.getBookings().remove(this);
        }
        this.customer = customer;
        if (customer != null && !customer.getBookings().contains(this)) {
            customer.getBookings().add(this);
        }
    }

    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }
    public List<Service> getServices() { return services; }
    public Admin getCreatedBy() { return createdBy; }
    public void setCreatedBy(Admin createdBy) { this.createdBy = createdBy; }
    public String getPaymentMethod() { return paymentMethod; }
    public boolean isPaid() { return isPaid; }
    public LocalDate getPaymentDate() { return paymentDate; }

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