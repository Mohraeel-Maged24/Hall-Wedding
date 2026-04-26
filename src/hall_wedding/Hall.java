package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class Hall implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String location;
    private int capacity;
    private double pricePerHour;

    @ManyToOne
    private Admin managedBy;

    public Hall() {}

    public Hall(String name, String location, int capacity, double pricePerHour) {
        setName(name);
        setLocation(location);
        setCapacity(capacity);
        setPricePerHour(pricePerHour);
    }

    // ─── Business Logic ─────────────────────

    public void updatePrice(double newPrice) {
        setPricePerHour(newPrice);
        System.out.println("Price updated to " + pricePerHour);
    }

    public void updateDetails() {
        System.out.println("Hall updated: " + this);
    }

    // ─── Getters & Setters ─────────────────

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Hall name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Hall name must be at least 3 characters");
        }
        if (!trimmed.matches(NAME_REGEX) || trimmed.replace(" ", "").isEmpty()) {
            throw new IllegalArgumentException("Hall name must contain letters only");
        }
        this.name = trimmed;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        this.location = location.trim();
    }

    public int getCapacity() { return capacity; }

    public void setCapacity(int capacity) {
        if (capacity < 10 || capacity > 10000) {
            throw new IllegalArgumentException("Capacity must be between 10 and 10000");
        }
        this.capacity = capacity;
    }

    public double getPricePerHour() { return pricePerHour; }

    public void setPricePerHour(double pricePerHour) {
        if (pricePerHour < 5000) {
            throw new IllegalArgumentException("Price must be at least 5000");
        }
        this.pricePerHour = pricePerHour;
    }

    public Admin getManagedBy() { return managedBy; }

    public void setManagedBy(Admin managedBy) { this.managedBy = managedBy; }

    @Override
    public String toString() {
        return "Hall[id=" + id +
                ", name=" + name +
                ", location=" + location +
                ", capacity=" + capacity +
                ", price=" + pricePerHour + "]";
    }
}