package hall_wedding;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Person implements Serializable {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    protected String name;
    protected String email;
    protected String password;

    public Person() {}

    public Person(String name, String email, String password) {
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public void logout() {
        System.out.println(name + " logged out.");
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Name must be at least 3 characters");
        }
        if (!trimmed.matches("^[\\p{L} ]+$")) {
            throw new IllegalArgumentException("Name must contain letters only");
        }
        if (trimmed.replace(" ", "").isEmpty()) {
            throw new IllegalArgumentException("Name cannot be spaces only");
        }
        this.name = trimmed;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !email.trim().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        this.password = password.trim();
    }
}