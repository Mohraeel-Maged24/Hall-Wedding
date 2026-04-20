package hall_wedding;

import jakarta.persistence.*;

public class JPAUtil {
    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("Hall_WeddingPU");

    public static EntityManager getEM() { return emf.createEntityManager(); }
    public static void close()          { emf.close(); }
}