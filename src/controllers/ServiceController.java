package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ServiceController {

    private static final String NAME_REGEX = "^[\\p{L} ]+$";

    public static List<Service> getAll() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT s FROM Service s", Service.class).getResultList();
        } finally { em.close(); }
    }

    public static List<Service> searchByNameOrType(String searchTerm) {
        EntityManager em = JPAUtil.getEM();
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) return getAll();
            String term = "%" + searchTerm.trim() + "%";
            return em.createQuery(
                "SELECT s FROM Service s WHERE LOWER(s.name) LIKE LOWER(:term) OR LOWER(s.type) LIKE LOWER(:term)",
                Service.class).setParameter("term", term).getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(s) FROM Service s", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public static List<Object[]> getMostRequestedServices() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery(
                "SELECT s.name, s.type, COUNT(s) AS cnt " +
                "FROM Booking b JOIN b.services s " +
                "WHERE b.status <> 'CANCELED' " +
                "GROUP BY s.name, s.type " +
                "ORDER BY cnt DESC",
                Object[].class)
                .getResultList();
        } finally { em.close(); }
    }
    public static Object[] getTopRequestedService() {
        EntityManager em = JPAUtil.getEM();
        try {
            List<Object[]> rows = em.createQuery(
                "SELECT s.name, s.type, COUNT(s) AS cnt " +
                "FROM Booking b JOIN b.services s " +
                "WHERE b.status <> 'CANCELED' " +
                "GROUP BY s.name, s.type " +
                "ORDER BY cnt DESC",
                Object[].class)
                .setMaxResults(1)
                .getResultList();
            return rows.isEmpty() ? null : rows.get(0);
        } finally { em.close(); }
    }

    public static List<Service> getServicesByType(String type) {
        EntityManager em = JPAUtil.getEM();
        try {
            if (type == null || type.trim().isEmpty()) return getAll();
            return em.createQuery(
                "SELECT s FROM Service s WHERE LOWER(s.type) = LOWER(:type) ORDER BY s.name",
                Service.class)
                .setParameter("type", type.trim())
                .getResultList();
        } finally { em.close(); }
    }

    public static void add(String name, double price, String type) {
        validateServiceData(name, price, type);
        EntityManager em = JPAUtil.getEM();
        try {
            Service s = new Service(name, price, type);
            if (LoginController.loggedAdmin != null) s.setAddedBy(LoginController.loggedAdmin);
            em.getTransaction().begin();
            em.persist(s);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void update(Long id, String name, double price, String type) {
        validateId(id);
        validateServiceData(name, price, type);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Service s = em.find(Service.class, id);
            if (s != null) { s.setName(name); s.setPrice(price); s.setType(type); }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void delete(Long id) {
        validateId(id);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Service s = em.find(Service.class, id);
            if (s != null) em.remove(s);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    private static void validateServiceData(String name, double price, String type) {
        if (name == null) throw new IllegalArgumentException("Service name is required");
        String trimmedName = name.trim();
        if (trimmedName.length() < 3) throw new IllegalArgumentException("Service name must be at least 3 characters");
        if (!trimmedName.matches(NAME_REGEX) || trimmedName.replace(" ", "").isEmpty())
            throw new IllegalArgumentException("Service name must contain letters only");
        if (type == null || type.trim().isEmpty()) throw new IllegalArgumentException("Service type is required");
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than 0");
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid id");
    }
}