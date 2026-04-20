package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.util.List;

public class CustomerController {

    public static List<Customer> getAll() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public static void add(String name, String email, String password, int phone, String ssn) {
        EntityManager em = JPAUtil.getEM();
        try {
            Customer c = new Customer(name, email, password, phone, ssn);
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void update(Long id, String name, String email, int phone) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer c = em.find(Customer.class, id);
            if (c != null) c.updateProfile(name, phone, email);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public static void delete(Long id) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer c = em.find(Customer.class, id);
            if (c != null) em.remove(c);
            em.getTransaction().commit();
        } finally { em.close(); }
    }
}