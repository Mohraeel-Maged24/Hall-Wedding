package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CustomerController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NAME_REGEX = "^[\\p{L} ]+$";
    private static final String PHONE_REGEX = "^(010|011|012|015)\\d{8}$";
    private static final String DIGITS_REGEX = "^\\d+$";

    public static List<Customer> getAll() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
        } finally { em.close(); }
    }

    public static List<Customer> searchByNameOrPhone(String searchTerm) {
        EntityManager em = JPAUtil.getEM();
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAll();
            }
            String term = "%" + searchTerm.trim() + "%";
            return em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(:term) OR c.phone LIKE :term",
                Customer.class)
                .setParameter("term", term)
                .getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public static void add(String name, String email, String password, String phone, String ssn) {
        ssn = normalizeSsn(ssn);
        validateForAdd(name, email, password, phone, ssn);
        validateUniqueSsn(ssn, null);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            syncPersonIdGeneratorIfNeeded(em);
            Customer c = new Customer(name, email, password, phone, ssn);
            em.persist(c);
            em.getTransaction().commit();
        } catch (PersistenceException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("ssn")) {
                throw new IllegalArgumentException("Database issue: SSN column is missing or not mapped correctly. Please check customer table schema.");
            }
            throw ex;
        } finally { em.close(); }
    }

//    public static void update(Long id, String name, String email, String phone) {
//        validateId(id);
//        validateForUpdate(name, email, phone);
//        EntityManager em = JPAUtil.getEM();
//        try {
//            em.getTransaction().begin();
//            Customer c = em.find(Customer.class, id);
//            if (c != null) c.updateProfile(name, phone, email);
//            em.getTransaction().commit();
//        } finally { em.close(); }
//    }

    public static void update(Long id, String name, String email, String password, String phone, String ssn) {
        validateId(id);
        ssn = normalizeSsn(ssn);
        validateForAdd(name, email, password, phone, ssn);
        validateUniqueSsn(ssn, id);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer c = em.find(Customer.class, id);
            if (c != null) {
                c.setName(name);
                c.setEmail(email);
                c.setPassword(password);
                c.setPhone(phone);
                c.setSsn(ssn);
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void delete(Long id) {
        validateId(id);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer c = em.find(Customer.class, id);
            if (c != null) {
                List<Booking> bookings = new java.util.ArrayList<>(c.getBookings());
                for (Booking booking : bookings) {
                    if (shouldCancelOnCustomerDelete(booking)) {
                        booking.cancelBooking(true);
                    }
                    booking.setCustomer(null);
                }
                em.flush();
                em.remove(c);
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    private static void validateForAdd(String name, String email, String password, String phone, String ssn) {
        validateName(name);
        validateEmail(email);
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        validatePhone(phone);
        if (ssn == null || !ssn.matches(DIGITS_REGEX) || ssn.length() != 14) {
            throw new IllegalArgumentException("SSN must be exactly 14 digits");
        }
    }

    private static void validateForUpdate(String name, String email, String phone) {
        validateName(name);
        validateEmail(email);
        validatePhone(phone);
    }

    private static void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Name must be at least 3 characters");
        }
        if (!trimmed.matches(NAME_REGEX)) {
            throw new IllegalArgumentException("Name must contain letters only");
        }
        if (trimmed.replace(" ", "").isEmpty()) {
            throw new IllegalArgumentException("Name cannot be spaces only");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !email.trim().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validatePhone(String phone) {
        if (phone == null || !phone.trim().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Phone must be 11 digits and start with 010, 011, 012, or 015");
        }
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }
    }

    private static boolean shouldCancelOnCustomerDelete(Booking booking) {
        if (booking == null || booking.getEventDate() == null) {
            return false;
        }
        if (!booking.getEventDate().isAfter(LocalDate.now())) {
            return false;
        }
        long daysToEvent = ChronoUnit.DAYS.between(LocalDate.now(), booking.getEventDate());
        return daysToEvent <= 30 && !"CONFIRMED".equalsIgnoreCase(booking.getStatus());
    }

    private static void validateUniqueSsn(String ssn, Long excludeId) {
        EntityManager em = JPAUtil.getEM();
        try {
            String jpql = "SELECT COUNT(c) FROM Customer c WHERE c.ssn = :ssn";
            if (excludeId != null) {
                jpql += " AND c.id <> :id";
            }
            var query = em.createQuery(jpql, Long.class).setParameter("ssn", ssn);
            if (excludeId != null) {
                query.setParameter("id", excludeId);
            }
            Long count = query.getSingleResult();
            if (count != null && count > 0) {
                throw new IllegalArgumentException("SSN already exists for another customer");
            }
        } finally {
            em.close();
        }
    }

    private static String normalizeSsn(String ssn) {
        if (ssn == null) {
            return null;
        }
        return ssn.trim();
    }

    private static void syncPersonIdGeneratorIfNeeded(EntityManager em) {
        Number maxPersonIdRaw = (Number) em.createNativeQuery(
            "SELECT COALESCE(MAX(id), 0) FROM person")
            .getSingleResult();
        long maxPersonId = maxPersonIdRaw == null ? 0L : maxPersonIdRaw.longValue();

        @SuppressWarnings("unchecked")
        List<Number> nextIdRows = em.createNativeQuery(
            "SELECT next_id FROM id_generator WHERE entity_name = 'person'")
            .getResultList();

        long requiredNextId = maxPersonId + 1;
        if (nextIdRows.isEmpty()) {
            em.createNativeQuery(
                "INSERT INTO id_generator (entity_name, next_id) VALUES ('person', ?)")
                .setParameter(1, requiredNextId)
                .executeUpdate();
            return;
        }

        long currentNextId = nextIdRows.get(0).longValue();
        if (currentNextId <= maxPersonId) {
            em.createNativeQuery(
                "UPDATE id_generator SET next_id = ? WHERE entity_name = 'person'")
                .setParameter(1, requiredNextId)
                .executeUpdate();
        }
    }
}