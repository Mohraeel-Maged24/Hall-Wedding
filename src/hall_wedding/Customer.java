package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private int phone;
    private String ssn;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public Customer() {}

    public Customer(String name, String email, String password,
                    int phone, String ssn) {
        super(name, email, password);
        this.phone = phone;
        this.ssn = ssn;
    }

    // Business Logic

    public void register() {
        System.out.println("Customer registered: " + name + " | SSN: " + ssn);
    }

    public void updateProfile(String name, int phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        System.out.println("Profile updated for: " + this.name);
    }

    public List<Booking> viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found for: " + name);
        } else {
            System.out.println("Bookings for " + name + ":");
            for (Booking b : bookings) {
                System.out.println(" - " + b);
            }
        }
        return bookings;
    }

    public void addBooking(Booking booking) {
        if (booking == null) {
            return;
        }
        if (!bookings.contains(booking)) {
            bookings.add(booking);
        }
        booking.setCustomer(this);
    }

    public void removeBooking(Booking booking) {
        if (booking == null) {
            return;
        }
        if (bookings.remove(booking)) {
            booking.setCustomer(null);
        }
    }

    // Getters & Setters

    public int getPhone() { return phone; }

    public void setPhone(int phone) { this.phone = phone; }

    public String getSsn() { return ssn; }

    public void setSsn(String ssn) { this.ssn = ssn; }

    public List<Booking> getBookings() { return bookings; }

    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    @Override
    public String toString() {
        return "Customer[id=" + id + ", name=" + name + ", phone=" + phone + "]";
    }
}