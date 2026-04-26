package controllers;

import hall_wedding.*;
import jakarta.persistence.*;
import java.util.List;

public class LoginController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static Customer loggedCustomer;
    public static Admin    loggedAdmin;

    public static boolean loginAsCustomer(String email, String password) {
        validateCredentials(email, password);
        EntityManager em = JPAUtil.getEM();
        try {
            List<Customer> res = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email=:e AND c.password=:p",
                Customer.class)
                .setParameter("e", email)
                .setParameter("p", password)
                .getResultList();

            if (!res.isEmpty()) {
                loggedCustomer = res.get(0);
                return true;
            }
            return false;
        } finally { em.close(); }
    }

    public static boolean loginAsAdmin(String email, String password) {
        validateCredentials(email, password);
        EntityManager em = JPAUtil.getEM();
        try {
            List<Admin> res = em.createQuery(
                "SELECT a FROM Admin a WHERE a.email=:e AND a.password=:p",
                Admin.class)
                .setParameter("e", email)
                .setParameter("p", password)
                .getResultList();

            if (!res.isEmpty()) {
                loggedAdmin = res.get(0);
                return true;
            }
            return false;
        } finally { em.close(); }
    }

    public static void logout() {
        loggedCustomer = null;
        loggedAdmin    = null;
    }

    public static int resetPasswordByEmail(String email, String newPassword) {
        validateEmailOnly(email);
        validateNewPassword(newPassword);

        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();

            int updatedCount = 0;

            List<Customer> customers = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :e",
                Customer.class)
                .setParameter("e", email.trim())
                .getResultList();
            for (Customer customer : customers) {
                customer.setPassword(newPassword);
                updatedCount++;
            }

            List<Admin> admins = em.createQuery(
                "SELECT a FROM Admin a WHERE a.email = :e",
                Admin.class)
                .setParameter("e", email.trim())
                .getResultList();
            for (Admin admin : admins) {
                admin.setPassword(newPassword);
                updatedCount++;
            }

            if (updatedCount == 0) {
                em.getTransaction().rollback();
                throw new IllegalArgumentException("No account found for this email");
            }

            em.getTransaction().commit();
            return updatedCount;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private static void validateCredentials(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.trim().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
    }

    private static void validateEmailOnly(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.trim().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validateNewPassword(String password) {
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
    }
}