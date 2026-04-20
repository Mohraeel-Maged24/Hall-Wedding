package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.util.List;

public class HallController {

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
        EntityManager em = JPAUtil.getEM();
        try {
            Hall hall = new Hall(name, location, capacity, price);
            if (LoginController.loggedAdmin != null)
                hall.setManagedBy(LoginController.loggedAdmin);
            em.getTransaction().begin();
            em.persist(hall);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void update(Long id, String name, String location, int capacity, double price) {
        EntityManager em = JPAUtil.getEM();
        try {
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
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Hall h = em.find(Hall.class, id);
            if (h != null) em.remove(h);
            em.getTransaction().commit();
        } finally { em.close(); }
    }
}