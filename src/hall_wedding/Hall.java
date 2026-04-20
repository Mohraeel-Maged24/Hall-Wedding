package hall_wedding;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class Hall implements Serializable {

    private static final long serialVersionUID = 1L;

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
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
    }

    // ─── Business Logic ─────────────────────

    public void updatePrice(double newPrice) {
        this.pricePerHour = newPrice;
        System.out.println("Price updated to " + pricePerHour);
    }

    public void updateDetails() {
        System.out.println("Hall updated: " + this);
    }

    // ─── Getters & Setters ─────────────────

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }

    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPricePerHour() { return pricePerHour; }

    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

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