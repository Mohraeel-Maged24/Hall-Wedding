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

    public static List<Hall> searchByNameAndLocation(String name, String location) {
        EntityManager em = JPAUtil.getEM();
        try {
            StringBuilder query = new StringBuilder("SELECT h FROM Hall h WHERE 1=1");
            if (name != null && !name.trim().isEmpty())         query.append(" AND LOWER(h.name) LIKE LOWER(:name)");
            if (location != null && !location.trim().isEmpty()) query.append(" AND h.location = :location");
            var q = em.createQuery(query.toString(), Hall.class);
            if (name != null && !name.trim().isEmpty())         q.setParameter("name", "%" + name.trim() + "%");
            if (location != null && !location.trim().isEmpty()) q.setParameter("location", location);
            return q.getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(h) FROM Hall h", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    
//    public static double[] getPriceRangeForHall(Long hallId) {
//        EntityManager em = JPAUtil.getEM();
//        try {
//            Object[] row = (Object[]) em.createQuery(
//                "SELECT MIN(b.totalPrice), MAX(b.totalPrice), AVG(b.totalPrice), COUNT(b) " +
//                "FROM Booking b " +
//                "WHERE b.hall.id = :hid AND b.status <> 'CANCELED' AND b.totalPrice > 0")
//                .setParameter("hid", hallId)
//                .getSingleResult();
//
//            if (row == null || row[3] == null || ((Long) row[3]) == 0) return null;
//            return new double[]{
//                ((Number) row[0]).doubleValue(),
//                ((Number) row[1]).doubleValue(),
//                ((Number) row[2]).doubleValue(),
//                ((Number) row[3]).doubleValue()
//            };
//        } finally { em.close(); }
//    }

    
    public static List<Object[]> getAllHallsBookingCount() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery(
                "SELECT h.name, (SELECT COUNT(b) FROM Booking b WHERE b.hall = h AND (b.status IS NULL OR b.status <> 'CANCELED')) " +
                "FROM Hall h " +
                "ORDER BY (SELECT COUNT(b2) FROM Booking b2 WHERE b2.hall = h AND (b2.status IS NULL OR b2.status <> 'CANCELED')) DESC",
                Object[].class)
                .getResultList();
        } finally { em.close(); }
    }

    public static long getBookingCountForHall(Long hallId) {
        EntityManager em = JPAUtil.getEM();
        try {
            Long result = em.createQuery(
                "SELECT COUNT(b) FROM Booking b WHERE b.hall.id = :hid AND b.status <> 'CANCELED'",
                Long.class)
                .setParameter("hid", hallId)
                .getSingleResult();
            return result != null ? result : 0L;
        } finally { em.close(); }
    }

    public static List<Hall> searchByPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice)
            throw new IllegalArgumentException("Invalid price range");
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery(
                "SELECT h FROM Hall h WHERE h.pricePerHour >= :min AND h.pricePerHour <= :max ORDER BY h.pricePerHour ASC",
                Hall.class)
                .setParameter("min", minPrice)
                .setParameter("max", maxPrice)
                .getResultList();
        } finally { em.close(); }
    }

    public static void add(String name, String location, int capacity, double price) {
        validateHallData(name, location, capacity, price);
        EntityManager em = JPAUtil.getEM();
        try {
            if (existsDuplicate(em, name, location, null))
                throw new IllegalArgumentException("This hall already exists with the same name and location");
            Hall hall = new Hall(name, location, capacity, price);
            if (LoginController.loggedAdmin != null) hall.setManagedBy(LoginController.loggedAdmin);
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
            if (existsDuplicate(em, name, location, id))
                throw new IllegalArgumentException("Another hall already exists with the same name and location");
            em.getTransaction().begin();
            Hall h = em.find(Hall.class, id);
            if (h != null) { h.setName(name); h.setLocation(location); h.setCapacity(capacity); h.setPricePerHour(price); }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void delete(Long id) {
        validateId(id);
        EntityManager em = JPAUtil.getEM();
        try {
            Long bookingCount = em.createQuery(
                "SELECT COUNT(b) FROM Booking b WHERE b.hall.id = :hid AND b.status <> 'CANCELED'",
                Long.class).setParameter("hid", id).getSingleResult();
            if (bookingCount != null && bookingCount > 0)
                throw new IllegalStateException("This hall has bookings and cannot be deleted");
            em.getTransaction().begin();
            Hall h = em.find(Hall.class, id);
            if (h != null) em.remove(h);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    private static void validateHallData(String name, String location, int capacity, double price) {
        if (name == null) throw new IllegalArgumentException("Hall name is required");
        String trimmedName = name.trim();
        if (trimmedName.length() < 3) throw new IllegalArgumentException("Hall name must be at least 3 characters");
        if (!trimmedName.matches(NAME_REGEX) || trimmedName.replace(" ", "").isEmpty())
            throw new IllegalArgumentException("Hall name must contain letters only");
        if (location == null || location.trim().isEmpty()) throw new IllegalArgumentException("Location is required");
        if (capacity < 10 || capacity > 10000) throw new IllegalArgumentException("Capacity must be between 10 and 10000");
        if (price < 5000) throw new IllegalArgumentException("Price must be at least 5000");
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid id");
    }

    private static boolean existsDuplicate(EntityManager em, String name, String location, Long excludeId) {
        String normalizedName     = name.trim().toLowerCase();
        String normalizedLocation = location.trim().toLowerCase();
        String jpql = "SELECT COUNT(h) FROM Hall h WHERE LOWER(h.name) = :name AND LOWER(h.location) = :location";
        if (excludeId != null) jpql += " AND h.id <> :id";
        var query = em.createQuery(jpql, Long.class)
            .setParameter("name", normalizedName)
            .setParameter("location", normalizedLocation);
        if (excludeId != null) query.setParameter("id", excludeId);
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }
}