package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Admin extends Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private double salary;
    private String shift;

    @OneToMany(mappedBy = "managedBy")
    private List<Hall> managedHalls = new ArrayList<>();

    @OneToMany(mappedBy = "addedBy")
    private List<Service> addedServices = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<Booking> createdBookings = new ArrayList<>();

    public Admin() {}

    public Admin(String name, String email, String password) {
        super(name, email, password);
    }

    public Admin(String name, String email, String password, double salary, String shift) {
        super(name, email, password);
        this.salary = salary;
        this.shift = shift;
    }

    // ─── Hall Management ─────────────────────────────

    public void addHall(List<Hall> hallList, Hall hall) {
        hallList.add(hall);
        hall.setManagedBy(this);
        System.out.println("Admin [" + name + "] added hall: " + hall.getName());
    }

    public void removeHall(List<Hall> hallList, Long hallId) {
        boolean removed = hallList.removeIf(h -> h.getId().equals(hallId));
        System.out.println(removed ? "Admin removed hall id=" + hallId : "Hall not found");
    }

    public void updateHallPrice(Hall hall, double newPrice) {
        hall.setPricePerHour(newPrice);
        System.out.println("Admin updated price for: " + hall.getName());
    }

    public void updateHallDetails(Hall hall, String name, String location, int capacity) {
        hall.setName(name);
        hall.setLocation(location);
        hall.setCapacity(capacity);
    }

    // ─── Service Management ─────────────────────────

    public void addService(List<Service> serviceList, Service service) {
        serviceList.add(service);
        service.setAddedBy(this);
        System.out.println("Admin added service: " + service.getName());
    }

    public void removeService(List<Service> serviceList, Long serviceId) {
        boolean removed = serviceList.removeIf(s -> s.getId().equals(serviceId));
        System.out.println(removed ? "Service removed" : "Service not found");
    }

    public void updateServicePrice(Service service, double newPrice) {
        service.updatePrice(newPrice);
    }

    // ─── Booking Management ─────────────────────────

    public Booking createBooking(Customer customer, Hall hall,
                                 java.time.LocalDate date, int durationHours) {
        Booking booking = new Booking(customer, hall, date, durationHours);
        booking.setCreatedBy(this);
        System.out.println("Booking created for " + customer.getName()
                + " at hall " + hall.getName());
        return booking;
    }

    // FIX 3: removeBooking كانت بتعمل setCustomer(null) من غير ما تعمل orphanRemoval
    // دلوقتي بنحسب الـ refund، بنـ cancel، وبنشيل من الـ customer list
    // الحذف الفعلي من الداتا بيز بيتم تلقائياً عبر orphanRemoval = true في Customer
    public void removeBooking(Customer customer, Booking booking) {
        if (booking.isPaid()) {
            booking.refundPayment();
        }
        booking.cancelBooking();
        customer.removeBooking(booking);
        booking.setCustomer(null);
        booking.setCreatedBy(null);
        System.out.println("Booking removed for " + customer.getName());
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public List<Hall> getManagedHalls() {
        return managedHalls;
    }

    public List<Service> getAddedServices() {
        return addedServices;
    }

    public List<Booking> getCreatedBookings() {
        return createdBookings;
    }

    @Override
    public String toString() {
        return "Admin[id=" + id + ", name=" + name + ", salary=" + salary + ", shift=" + shift + "]";
    }
}