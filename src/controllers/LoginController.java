package controllers;

import hall_wedding.*;
import jakarta.persistence.*;
import java.util.List;

public class LoginController {

    public static Customer loggedCustomer;
    public static Admin    loggedAdmin;

    public static boolean loginAsCustomer(String email, String password) {
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
}