package controllers;

import hall_wedding.*;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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
            List<Booking> bookings = em.createNamedQuery("Booking.findByCustomer", Booking.class)
                .setParameter("customerId", customerId)
                .getResultList();
            bookings.sort(Comparator.comparing(Booking::getEventDate).reversed());
            return bookings;
        } finally { em.close(); }
    }

    public static List<Booking> getByStatus(String status) {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createNamedQuery("Booking.findByStatus", Booking.class)
                .setParameter("status", status)
                .getResultList();
        } finally { em.close(); }
    }

    public static List<Booking> getByDateRangeAndHall(LocalDate fromDate, LocalDate toDate, Hall hall) {
        if (fromDate == null || toDate == null)
            throw new IllegalArgumentException("From date and To date are required");
        if (fromDate.isAfter(toDate))
            throw new IllegalArgumentException("From date cannot be after To date");

        EntityManager em = JPAUtil.getEM();
        try {
            String query;
            if (hall == null || hall.getId() == null) {
                query = "SELECT b FROM Booking b WHERE b.eventDate BETWEEN :from AND :to ORDER BY b.eventDate DESC";
            } else {
                query = "SELECT b FROM Booking b WHERE b.hall.id = :hallId AND b.eventDate BETWEEN :from AND :to ORDER BY b.eventDate DESC";
            }
            var q = em.createQuery(query, Booking.class);
            q.setParameter("from", fromDate);
            q.setParameter("to", toDate);
            if (hall != null && hall.getId() != null) q.setParameter("hallId", hall.getId());
            return q.getResultList();
        } finally { em.close(); }
    }

    public static long count() {
        EntityManager em = JPAUtil.getEM();
        try {
            return em.createQuery("SELECT COUNT(b) FROM Booking b", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public static double getTotalRevenueByMonth(int month, int year) {
        double total = 0.0;
        for (Booking b : getAll()) {
            if (b.getEventDate() != null
                    && b.getEventDate().getMonthValue() == month
                    && b.getEventDate().getYear() == year) {
                total += b.getNetRevenueAmount();
            }
        }
        return total;
    }

    public static Object[] getMostBookedHall() {
        EntityManager em = JPAUtil.getEM();
        try {
            List<Object[]> rows = em.createQuery(
                "SELECT b.hall.name, COUNT(b) AS cnt FROM Booking b " +
                "WHERE b.status <> 'CANCELED' " +
                "GROUP BY b.hall.name " +
                "ORDER BY cnt DESC",
                Object[].class)
                .setMaxResults(1)
                .getResultList();
            return rows.isEmpty() ? null : rows.get(0);
        } finally { em.close(); }
    }

    public static long countByCustomer(Long customerId) {
        EntityManager em = JPAUtil.getEM();
        try {
            Long result = em.createQuery(
                "SELECT COUNT(b) FROM Booking b WHERE b.customer.id = :cid",
                Long.class)
                .setParameter("cid", customerId)
                .getSingleResult();
            return result != null ? result : 0L;
        } finally { em.close(); }
    }

//    public static double create(Customer customer, Hall hall,
//                                LocalDate date, int durationHours,
//                                List<Service> services) {
//        return create(customer, hall, date, durationHours, services, 0.0);
//    }
//
//    public static double create(Customer customer, Hall hall,
//                                LocalDate date, int durationHours,
//                                List<Service> services,
//                                double paidAmount) {
//        return create(customer, hall, date, durationHours, services, paidAmount, null);
//    }

    public static double create(Customer customer, Hall hall,
                                LocalDate date, int durationHours,
                                List<Service> services,
                                double paidAmount, String paymentMethod) {
        validateCreateInputs(customer, hall, date, durationHours, services);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Customer mc = em.merge(customer);
            Hall     mh = em.merge(hall);
            
            List<Booking> conflicts = findReplaceableConflicts(em, mh.getId(), date);
            
            Booking b = new Booking(mc, mh, date, durationHours);
            for (Service service : services) b.addService(em.merge(service));
            if (LoginController.loggedAdmin != null)
                b.setCreatedBy(em.merge(LoginController.loggedAdmin));

            b.calculateTotalPrice();
            double total = b.getTotalPrice();
            double minDeposit = total * 0.20;
            long daysToEvent = ChronoUnit.DAYS.between(LocalDate.now(), date);
            boolean withinOneMonth = daysToEvent >= 0 && daysToEvent <= 30;

            if (paymentMethod == null || paymentMethod.trim().isEmpty())
                throw new IllegalArgumentException("Payment method is required");
            if (paidAmount <= 0)
                throw new IllegalArgumentException("Paid amount is required");
            
            if (withinOneMonth || !conflicts.isEmpty()) {
                if (paidAmount < total) {
                    throw new IllegalArgumentException(
                        String.format("❌ Booking is within 30 days, or the hall has a pending booking (< 30 days to event). " +
                        "You MUST pay the full amount (%.0f EGP), not just deposit (%.0f EGP)", 
                        total, minDeposit));
                }
            } else {
                // No conflicts - normal deposit validation
                if (paidAmount < minDeposit)
                    throw new IllegalArgumentException(
                        String.format("Minimum payment is 20%% of total (%.0f EGP)", minDeposit));
            }

            b.registerDeposit(paymentMethod, paidAmount);
            // Cancel replaceable PENDING bookings with 20% penalty before saving the new booking
//            if (!conflicts.isEmpty()) {
//                for (Booking conflictBooking : conflicts) {
//                    conflictBooking.cancelForReplacement(0.20);
//                }
//            }

            em.persist(b);
            em.getTransaction().commit();
            return b.getTotalPrice();
        } finally { em.close(); }
        
    }

    public static boolean pay(Long bookingId, String paymentMethod) {
        validateId(bookingId);
        if (paymentMethod == null || paymentMethod.trim().isEmpty())
            throw new IllegalArgumentException("Payment method is required");
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Booking b = em.find(Booking.class, bookingId);
            boolean done = (b != null) && b.processPayment(paymentMethod);
            em.getTransaction().commit();
            return done;
        } finally { em.close(); }
    }

//    public static double update(Long bookingId, Customer customer, Hall hall,
//                                LocalDate date, int durationHours,
//                                List<Service> services,
//                                String paymentMethod) {
//        return update(bookingId, customer, hall, date, durationHours, services, 0.0, paymentMethod);
//    }

    public static double update(Long bookingId, Customer customer, Hall hall,
                                LocalDate date, int durationHours,
                                List<Service> services,
                                double additionalPaidAmount,
                                String paymentMethod) {
        validateId(bookingId);
        validateCreateInputs(customer, hall, date, durationHours, services);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Booking booking = em.find(Booking.class, bookingId);
            if (booking == null)
                throw new IllegalArgumentException("Booking not found");
            if ("CANCELED".equals(booking.getStatus()))
                throw new IllegalArgumentException("Canceled booking cannot be updated");
//            if (booking.isPaid())
//                throw new IllegalArgumentException("Paid booking cannot be updated");

            Customer managedCustomer = em.merge(customer);
            Hall     managedHall     = em.merge(hall);
            ensureHallDateAvailability(em, managedHall.getId(), date, bookingId);

            boolean immediateFullPayment = paymentMethod != null && !paymentMethod.trim().isEmpty();
            int canceledPending = immediateFullPayment
                ? cancelPendingBookings(em, managedHall.getId(), date, bookingId, true)
                : 0;

            booking.setCustomer(managedCustomer);
            booking.setHall(managedHall);
            // Use setEventDate — this is a deliberate user update so validation is correct
            booking.setEventDate(date);
            booking.setDurationHours(durationHours);
            booking.getServices().clear();
            for (Service service : services) booking.addService(em.merge(service));
            booking.calculateTotalPrice();

            if (additionalPaidAmount > 0.0) {
                if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                    throw new IllegalArgumentException("Payment method is required");
                }
                double minExtra = booking.getRequiredDepositAmount();
                if (!booking.isPaid() && additionalPaidAmount < minExtra && booking.getGrossPaidAmount() <= 0.0) {
                    throw new IllegalArgumentException("Minimum payment is 20% of total");
                }
                booking.addPayment(paymentMethod, additionalPaidAmount);
            } else if (immediateFullPayment) {
                if (booking.getGrossPaidAmount() <= 0.0) {
                    booking.registerDeposit(paymentMethod, booking.getTotalPrice());
                } else {
                    booking.addPayment(paymentMethod, booking.getRemainingAmount());
                }
            } else if (canceledPending > 0) {
                booking.cancelBooking(false);
            }

            em.getTransaction().commit();
            return booking.getTotalPrice();
        } finally { em.close(); }
    }

    // Backwards-compatible overload
//    public static double update(Long bookingId, Customer customer, Hall hall,
//                                LocalDate date, int durationHours,
//                                List<Service> services) {
//        return update(bookingId, customer, hall, date, durationHours, services, 0.0, null);
//    }

    public static void cancel(Long bookingId) {
        validateId(bookingId);
        EntityManager em = JPAUtil.getEM();
        try {
            em.getTransaction().begin();
            Booking b = em.find(Booking.class, bookingId);
            if (b != null) {
                b.cancelBooking(true);
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    private static void validateCreateInputs(Customer customer, Hall hall,
                                             LocalDate date, int durationHours,
                                             List<Service> services) {
        if (customer == null)  throw new IllegalArgumentException("Customer is required");
        if (hall == null)      throw new IllegalArgumentException("Hall is required");
        if (date == null)      throw new IllegalArgumentException("Event date is required");
        if (!date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Event date must be after today");
        if (durationHours < 1 || durationHours > 24)
            throw new IllegalArgumentException("Duration must be between 1 and 24 hours");
    }

    private static void ensureHallDateAvailability(EntityManager em, Long hallId,
                                                   LocalDate date, Long excludedBookingId) {
        List<Booking> conflicts;
        if (excludedBookingId == null) {
            conflicts = em.createQuery(
                "SELECT b FROM Booking b WHERE b.hall.id = :hid AND b.eventDate = :date AND b.status <> 'CANCELED'",
                Booking.class)
                .setParameter("hid", hallId)
                .setParameter("date", date)
                .getResultList();
        } else {
            conflicts = em.createQuery(
                "SELECT b FROM Booking b WHERE b.hall.id = :hid AND b.eventDate = :date AND b.status <> 'CANCELED' AND b.id <> :bid",
                Booking.class)
                .setParameter("hid", hallId)
                .setParameter("date", date)
                .setParameter("bid", excludedBookingId)
                .getResultList();
        }

        if (conflicts.isEmpty()) {
            System.out.println(">>> Hall available (no conflicts)");
            return;
        }

        System.out.println(">>> Found " + conflicts.size() + " conflicting bookings");

        // Check if ALL conflicts are replaceable:
        // replaceable = PENDING + not fully paid + event is within 30 days
        LocalDate now = LocalDate.now();
        boolean allReplaceable = true;
        for (Booking b : conflicts) {
            long daysToEvent = ChronoUnit.DAYS.between(now, b.getEventDate());
            boolean withinOneMonth = daysToEvent >= 0 && daysToEvent <= 30;
            boolean isPending = "PENDING".equals(b.getStatus());
            boolean notFullyPaid = b.getGrossPaidAmount() < b.getTotalPrice();
            boolean replaceable = isPending && notFullyPaid && withinOneMonth;
            
            System.out.println(">>> Conflict Check - Booking #" + b.getId() 
                + " | Status: " + b.getStatus() 
                + " | Paid: " + b.getGrossPaidAmount() + "/" + b.getTotalPrice()
                + " | Days: " + daysToEvent 
                + " | Replaceable: " + replaceable);
            
            if (!replaceable) {
                allReplaceable = false;
                break;
            }
        }

        if (!allReplaceable) {
            throw new IllegalArgumentException("❌ This hall already has a confirmed/non-replaceable booking on the same day");
        }

        // Cancel the replaceable pending bookings with 20% penalty
        System.out.println(">>> Canceling all conflicting PENDING bookings with 20% penalty...");
        for (Booking b : conflicts) {
            b.cancelForReplacement(0.20); // Changed from cancelBooking(false) to apply 20% penalty
            System.out.println(">>> Canceled Booking #" + b.getId() + " for replacement");
        }
    }

    /**
     * Find replaceable conflicting bookings (for validation before creating new booking)
     * replaceable = PENDING + not fully paid + event is within 30 days
     */
    private static List<Booking> findReplaceableConflicts(EntityManager em, Long hallId, LocalDate date) {
        List<Booking> allConflicts = em.createQuery(
            "SELECT b FROM Booking b WHERE b.hall.id = :hid AND b.eventDate = :date AND b.status <> 'CANCELED'",
            Booking.class)
            .setParameter("hid", hallId)
            .setParameter("date", date)
            .getResultList();

        LocalDate now = LocalDate.now();
        List<Booking> replaceable = new ArrayList<>();
        
        for (Booking b : allConflicts) {
            long daysToEvent = ChronoUnit.DAYS.between(now, b.getEventDate());
            boolean withinOneMonth = daysToEvent >= 0 && daysToEvent <= 30;
            boolean isPending = "PENDING".equals(b.getStatus());
            boolean notFullyPaid = b.getGrossPaidAmount() < b.getTotalPrice();
            boolean isReplaceable = isPending && notFullyPaid && withinOneMonth;
            
            if (isReplaceable) {
                replaceable.add(b);
            }
        }
        
        if (!replaceable.isEmpty()) {
            System.out.println(">>> Found " + replaceable.size() + " replaceable conflicts on " + date);
        }
        
        return replaceable;
    }

    private static int cancelPendingBookings(EntityManager em, Long hallId, LocalDate date,
                                             Long excludedBookingId, boolean systemReplacement) {
        List<Booking> pendings = em.createQuery(
            "SELECT b FROM Booking b WHERE b.hall.id = :hid AND b.eventDate = :date AND b.status = 'PENDING'",
            Booking.class)
            .setParameter("hid", hallId)
            .setParameter("date", date)
            .getResultList();

        int canceled = 0;
        for (Booking pending : pendings) {
            if (excludedBookingId != null && excludedBookingId.equals(pending.getId())) continue;
            pending.cancelBooking(!systemReplacement);
            canceled++;
        }
        return canceled;
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid id");
    }
}