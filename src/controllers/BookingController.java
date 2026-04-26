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
                                LocalDate date, int durationHours,
                                List<Service> services) {
        validateCreateInputs(customer, hall, date, durationHours, services);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer mc = em.merge(customer);
            Hall     mh = em.merge(hall);
            if (hasSameHallBooking(em, mh.getId(), date)) {
                throw new IllegalArgumentException("This hall already has a booking on the same day");
            }
            Booking  b  = new Booking(mc, mh, date, durationHours);
            for (Service service : services) {
                Service managedService = em.merge(service);
                b.addService(managedService);
            }
            b.setStatus("PENDING");
            if (LoginController.loggedAdmin != null)
                b.setCreatedBy(em.merge(LoginController.loggedAdmin));
            em.persist(b);
            em.getTransaction().commit();
            return b.getTotalPrice();
        } finally { em.close(); }
    }

    public static boolean pay(Long bookingId, String paymentMethod) {
        validateId(bookingId);
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Booking b = em.find(Booking.class, bookingId);
            if (b != null) {
                b.setPaymentMethod(paymentMethod);
            }
            boolean done = (b != null) && b.processPayment(paymentMethod);
            em.getTransaction().commit();
            return done;
        } finally { em.close(); }
    }

    public static void cancel(Long bookingId) {
        validateId(bookingId);
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

    private static void validateCreateInputs(Customer customer, Hall hall,
                                             LocalDate date, int durationHours,
                                             List<Service> services) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (hall == null) {
            throw new IllegalArgumentException("Hall is required");
        }
        if (date == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
        if (durationHours < 1 || durationHours > 24) {
            throw new IllegalArgumentException("Duration must be between 1 and 24 hours");
        }
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one service");
        }
    }

    private static boolean hasSameHallBooking(EntityManager em, Long hallId, LocalDate date) {
        Long count = em.createQuery(
            "SELECT COUNT(b) FROM Booking b WHERE b.hall.id = :hid AND b.eventDate = :date AND b.status <> 'CANCELED'",
            Long.class)
            .setParameter("hid", hallId)
            .setParameter("date", date)
            .getSingleResult();
        return count != null && count > 0;
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }
    }
}