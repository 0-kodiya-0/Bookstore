package com.example.bookstore.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAConfig {
    
    private static final Logger LOGGER = Logger.getLogger(JPAConfig.class.getName());
    private static EntityManagerFactory entityManagerFactory;
    
    static {
        try {
            LOGGER.info("Initializing JPA EntityManagerFactory...");
            
            // Determine which persistence unit to use based on environment
            String persistenceUnit = determinePersistenceUnit();
            LOGGER.info("Using persistence unit: " + persistenceUnit);
            
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
            LOGGER.info("JPA EntityManagerFactory initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize JPA EntityManagerFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * Determines which persistence unit to use based on system properties or environment variables.
     * Priority order:
     * 1. System property: -Ddb.type=mysql
     * 2. Environment variable: DB_TYPE=postgresql
     * 3. Default: bookstore-h2-pu (H2 in-memory for development)
     */
    private static String determinePersistenceUnit() {
        // Check system property first
        String dbType = System.getProperty("db.type");
        
        // If not found, check environment variable
        if (dbType == null || dbType.trim().isEmpty()) {
            dbType = System.getenv("DB_TYPE");
        }
        
        // If still not found, use default
        if (dbType == null || dbType.trim().isEmpty()) {
            dbType = "h2"; // Default to H2 for development
        }
        
        dbType = dbType.toLowerCase().trim();
        
        switch (dbType) {
            case "mysql":
                return "bookstore-mysql-pu";
            case "postgresql":
            case "postgres":
                return "bookstore-postgresql-pu";
            case "h2":
            default:
                return "bookstore-h2-pu"; // H2 default
        }
    }
    
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("EntityManagerFactory is not initialized");
        }
        return entityManagerFactory.createEntityManager();
    }
    
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            LOGGER.info("Closing JPA EntityManagerFactory...");
            entityManagerFactory.close();
            LOGGER.info("JPA EntityManagerFactory closed");
        }
    }
    
    // Utility method for executing operations with proper transaction handling
    public static <T> T executeInTransaction(JPAOperation<T> operation) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T result = operation.execute(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            em.close();
        }
    }
    
    // Utility method for executing operations without return value
    public static void executeInTransaction(JPAVoidOperation operation) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            operation.execute(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            em.close();
        }
    }
    
    // Utility method for read-only operations
    public static <T> T executeReadOnly(JPAOperation<T> operation) {
        EntityManager em = getEntityManager();
        try {
            return operation.execute(em);
        } finally {
            em.close();
        }
    }
    
    @FunctionalInterface
    public interface JPAOperation<T> {
        T execute(EntityManager em);
    }
    
    @FunctionalInterface
    public interface JPAVoidOperation {
        void execute(EntityManager em);
    }
}