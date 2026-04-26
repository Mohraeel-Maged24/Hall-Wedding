/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hall_wedding;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class Service implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    private String name;
    private double price;
    private String type;

    @ManyToOne
    private Admin addedBy;

    public Service() {}

    public Service(String name, double price) {
        setName(name);
        setPrice(price);
    }

    public Service(String name, double price, String type) {
        setName(name);
        setPrice(price);
        setType(type);
    }

    public void updatePrice(double newPrice) {
        setPrice(newPrice);
        System.out.println("Service price updated to " + newPrice);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Service name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Service name must be at least 3 characters");
        }
        if (!trimmed.matches(NAME_REGEX) || trimmed.replace(" ", "").isEmpty()) {
            throw new IllegalArgumentException("Service name must contain letters only");
        }
        this.name = trimmed;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Service type is required");
        }
        this.type = type.trim();
    }

    public Admin getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Admin addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Service)) {
            return false;
        }
        Service other = (Service) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Service[id=" + id + ", name=" + name + ", price=" + price + ", type=" + type + "]";
    }
}

