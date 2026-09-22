package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.util.List;

public class AdminController {

    public static long hallCount()     { return HallController.count();     }
    public static long serviceCount()  { return ServiceController.count();  }
    public static long bookingCount()  { return BookingController.count();  }
    public static long customerCount() { return CustomerController.count(); }


    public static List<Admin> getAllAdmins() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT a FROM Admin a", Admin.class).getResultList();
        } finally { em.close(); }
    }

    public static List<Booking> getBookingsByAdmin(Long adminId) {
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Invalid admin id");
        }
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery(
                "SELECT b FROM Booking b WHERE b.createdBy.id = :adminId ORDER BY b.eventDate DESC",
                Booking.class)
                .setParameter("adminId", adminId)
                .getResultList();
        } finally { em.close(); }
    }
}