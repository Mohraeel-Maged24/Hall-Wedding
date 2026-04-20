package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ServiceController {

    public static List<Service> getAll() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT s FROM Service s", Service.class).getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(s) FROM Service s", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public static void add(String name, double price, String type) {
        EntityManager em = JPAUtil.getEM();
        try {
            Service s = new Service(name, price, type);
            if (LoginController.loggedAdmin != null)
                s.setAddedBy(LoginController.loggedAdmin);
            em.getTransaction().begin();
            em.persist(s);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void update(Long id, String name, double price, String type) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Service s = em.find(Service.class, id);
            if (s != null) {
                s.setName(name);
                s.setPrice(price);
                s.setType(type);
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void delete(Long id) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Service s = em.find(Service.class, id);
            if (s != null) em.remove(s);
            em.getTransaction().commit();
        } finally { em.close(); }
    }
}