package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.util.List;

public class HallController {

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    public static List<Hall> getAll() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT h FROM Hall h", Hall.class).getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(h) FROM Hall h", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public static void add(String name, String location, int capacity, double price) {
        validateHallData(name, location, capacity, price);
        EntityManager em = JPAUtil.getEM();
        try {
            if (existsDuplicate(em, name, location, null)) {
                throw new IllegalArgumentException("This hall already exists with the same name and location");
            }
            Hall hall = new Hall(name, location, capacity, price);
            if (LoginController.loggedAdmin != null)
                hall.setManagedBy(LoginController.loggedAdmin);
            em.getTransaction().begin();
            em.persist(hall);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void update(Long id, String name, String location, int capacity, double price) {
        validateId(id);
        validateHallData(name, location, capacity, price);
        EntityManager em = JPAUtil.getEM();
        try {
            if (existsDuplicate(em, name, location, id)) {
                throw new IllegalArgumentException("Another hall already exists with the same name and location");
            }
            em.getTransaction().begin();
            Hall h = em.find(Hall.class, id);
            if (h != null) {
                h.setName(name);
                h.setLocation(location);
                h.setCapacity(capacity);
                h.setPricePerHour(price);
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void delete(Long id) {
        validateId(id);
        EntityManager em = JPAUtil.getEM();
        try {
            Long bookingCount = em.createQuery(
                "SELECT COUNT(b) FROM Booking b WHERE b.hall.id = :hid AND b.status <> 'CANCELED'",
                Long.class)
                .setParameter("hid", id)
                .getSingleResult();
            if (bookingCount != null && bookingCount > 0) {
                throw new IllegalStateException("This hall has bookings and cannot be deleted");
            }

            em.getTransaction().begin();
            Hall h = em.find(Hall.class, id);
            if (h != null) em.remove(h);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    private static void validateHallData(String name, String location, int capacity, double price) {
        if (name == null) {
            throw new IllegalArgumentException("Hall name is required");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 3) {
            throw new IllegalArgumentException("Hall name must be at least 3 characters");
        }
        if (!trimmedName.matches(NAME_REGEX) || trimmedName.replace(" ", "").isEmpty()) {
            throw new IllegalArgumentException("Hall name must contain letters only");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (capacity < 10 || capacity > 10000) {
            throw new IllegalArgumentException("Capacity must be between 10 and 10000");
        }
        if (price < 5000) {
            throw new IllegalArgumentException("Price must be at least 5000");
        }
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }
    }

    private static boolean existsDuplicate(EntityManager em, String name, String location, Long excludeId) {
        String normalizedName = name.trim().toLowerCase();
        String normalizedLocation = location.trim().toLowerCase();

        String jpql = "SELECT COUNT(h) FROM Hall h WHERE LOWER(h.name) = :name AND LOWER(h.location) = :location";
        if (excludeId != null) {
            jpql += " AND h.id <> :id";
        }

        var query = em.createQuery(jpql, Long.class)
            .setParameter("name", normalizedName)
            .setParameter("location", normalizedLocation);
        if (excludeId != null) {
            query.setParameter("id", excludeId);
        }

        Long count = query.getSingleResult();
        return count != null && count > 0;
    }
}