package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class BookingController {

    public static List<Booking> getAll() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
        } finally { em.close(); }
    }

    public static List<Booking> getByCustomer(Long customerId) {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery(
                "SELECT b FROM Booking b WHERE b.customer.id = :cid ORDER BY b.eventDate DESC",
                Booking.class)
                .setParameter("cid", customerId)
                .getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(b) FROM Booking b", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    // بترجع الـ total price بعد ما تعمل commit
    public static double create(Customer customer, Hall hall,
                                LocalDate date, int durationHours) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer mc = em.merge(customer);
            Hall     mh = em.merge(hall);
            Booking  b  = new Booking(mc, mh, date, durationHours);
            b.confirmBooking();
            if (LoginController.loggedAdmin != null)
                b.setCreatedBy(em.merge(LoginController.loggedAdmin));
            em.persist(b);
            em.getTransaction().commit();
            return b.getTotalPrice();
        } finally { em.close(); }
    }

    public static boolean pay(Long bookingId) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Booking b = em.find(Booking.class, bookingId);
            boolean done = (b != null) && b.processPayment("CASH");
            em.getTransaction().commit();
            return done;
        } finally { em.close(); }
    }

    public static void cancel(Long bookingId) {
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Booking b = em.find(Booking.class, bookingId);
            if (b != null) {
                if (b.isPaid()) b.refundPayment();
                b.cancelBooking();
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }
}