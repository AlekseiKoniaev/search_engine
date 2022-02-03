package repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import repository.DBStructure.Page;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;
    private static Session session;
    private static Transaction transaction;
    private static SessionFactory factory;
    
    public static void init() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(registry).buildMetadata();
    
        factory = metadata.buildSessionFactory();
        session = factory.openSession();
        
    }
    
    public static void close() {
        factory.close();
    }
    
    public static Connection getConnection() {
        return connection;
    }
    
    public static void insertPage(Page page) {
        String content = page.getContent() == null ? "" : page.getContent()
                .replaceAll("\\s*\\n\\s*", "")  // удаление переносов строки
                .replaceAll("'", "\\\\'");      // экранирование символа
        String sql = "INSERT INTO page(path, code, content) " +
                "VALUES ('" + page.getPath() + "', " + page.getCode() + ", '" + content + "')";
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void insertPageHibernate(Page page) {
        transaction = session.beginTransaction();
        session.save(page);
        transaction.commit();
    
    }
}
