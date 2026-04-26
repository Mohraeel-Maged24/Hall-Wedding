package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends Person implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DIGITS_REGEX = "^\\d+$";

    private String phone;
    private String ssn;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public Customer() {}

    public Customer(String name, String email, String password,
                    String phone, String ssn) {
        super(name, email, password);
        setPhone(phone);
        setSsn(ssn);
    }

    // Business Logic

    public void register() {
        System.out.println("Customer registered: " + name + " | SSN: " + ssn);
    }

    public void updateProfile(String name, String phone, String email) {
        setName(name);
        setPhone(phone);
        setEmail(email);
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

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone is required");
        }
        String phoneText = phone.trim();
        if (!phoneText.matches("^(010|011|012|015)\\d{8}$")) {
            throw new IllegalArgumentException("Phone must be 11 digits and start with 010, 011, 012, or 015");
        }
        this.phone = phoneText;
    }

    public String getSsn() { return ssn; }

    public void setSsn(String ssn) {
        if (ssn == null || !ssn.matches(DIGITS_REGEX) || ssn.length() != 14) {
            throw new IllegalArgumentException("SSN must be exactly 14 digits");
        }
        this.ssn = ssn;
    }

    public List<Booking> getBookings() { return bookings; }

    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    @Override
    public String toString() {
        return "Customer[id=" + id + ", name=" + name + ", phone=" + phone + "]";
    }
}