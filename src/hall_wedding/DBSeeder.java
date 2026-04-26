package hall_wedding;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;

public final class DBSeeder {

    private DBSeeder() {
    }

    public static void seedIfEmpty() {
        EntityManager em = JPAUtil.getEM();
        try {
            long customers = em.createQuery("SELECT COUNT(c) FROM Customer c", Long.class).getSingleResult();
            long admins = em.createQuery("SELECT COUNT(a) FROM Admin a", Long.class).getSingleResult();
            if (customers > 0 || admins > 0) {
                return;
            }

            em.getTransaction().begin();

            Admin admin1 = new Admin("System Admin", "admin@hall.com", "123456", 12000, "Morning");
            Admin admin2 = new Admin("Night Admin", "admin2@hall.com", "123456", 11000, "Night");

            Customer c1 = new Customer("Ahmed Ali", "ahmed@mail.com", "1234", "01010101010", "30001010101010");
            Customer c2 = new Customer("Sara Hassan", "sara@mail.com", "1234", "01120202020", "30002020202020");
            Customer c3 = new Customer("Mona Samy", "mona@mail.com", "1234", "01230303030", "30003030303030");

            Hall h1 = new Hall("Royal Hall", "Cairo", 250, 5000);
            h1.setManagedBy(admin1);
            Hall h2 = new Hall("Golden Palace", "Giza", 350, 6200);
            h2.setManagedBy(admin1);
            Hall h3 = new Hall("Nile View", "Alexandria", 180, 5500);
            h3.setManagedBy(admin2);

            Service s1 = new Service("Premium Catering", 12000, "Catering");
            s1.setAddedBy(admin1);
            Service s2 = new Service("DJ & Sound", 4000, "Music");
            s2.setAddedBy(admin1);
            Service s3 = new Service("Floral Decoration", 5500, "Decor");
            s3.setAddedBy(admin2);

            Booking b1 = new Booking(c1, h1, LocalDate.now().plusDays(15), 5);
            b1.setCreatedBy(admin1);
            b1.setPaymentMethod("Cash");
            b1.addService(s1);
            b1.addService(s3);
            b1.confirmBooking();
            b1.processPayment("Cash");

            Booking b2 = new Booking(c2, h2, LocalDate.now().plusDays(30), 6);
            b2.setCreatedBy(admin1);
            b2.setPaymentMethod("Cash");
            b2.addService(s2);
            b2.confirmBooking();

            em.persist(admin1);
            em.persist(admin2);

            em.persist(c1);
            em.persist(c2);
            em.persist(c3);

            em.persist(h1);
            em.persist(h2);
            em.persist(h3);

            em.persist(s1);
            em.persist(s2);
            em.persist(s3);

            em.persist(b1);
            em.persist(b2);

            em.getTransaction().commit();
            System.out.println("Sample data inserted successfully.");
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}