package main.repository.control;

import main.model.Field;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HibernateConnection {
    
    private static SessionFactory sessionFactory;
    private static Session session;
    
    
    public static void init() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        sessionFactory = metadata.getSessionFactoryBuilder().build();
        session = sessionFactory.openSession();
        fillField();
    }
    
    private static void fillField() {
        List<Field> fields = new ArrayList<>(List.of(
                new Field("title", "title", 1.0f),
                new Field("body", "body", 0.8f)
        ));
        fields.forEach(HibernateConnection::insertField);
    }
    
    private static void insertField(Field field){
        session.save(field);
    }
    
    public static void close() {
        sessionFactory.close();
    }
    
    public static List<Field> getFields() {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Field> query = builder.createQuery(Field.class);
        Root<Field> root = query.from(Field.class);
        query.select(root);
    
        return session.createQuery(query).getResultList();
    }
    
    
    public static int getPageCount() {
        String query = "SELECT count(*) AS count FROM page";
        return session.createNativeQuery(query, Integer.class).getSingleResult();
    }
    
    public static Page getPageById(int pageId) {
        return session.get(Page.class, pageId);
    }
    
    public static Page getPageByPath(String path) {
        String query = "SELECT p FROM page p WHERE p.path = '" + path + "'";
        return session.createQuery(query, Page.class).getSingleResult();
    }
    
    public static Lemma getLemmaById(int lemmaId) {
        return session.get(Lemma.class, lemmaId);
    }
    
    public static Lemma getLemmaByLemma(String lemma) {
        String query = "SELECT l FROM lemma l WHERE l.lemma = '" + lemma + "'";
        try {
            return session.createQuery(query, Lemma.class).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static List<Lemma> getLemmas(Set<String> lemmas) {
        return lemmas.stream().map(HibernateConnection::getLemmaByLemma).toList();
    }
    
    
    public static List<Index> findIndexes(int id, String field) {
        String query = "SELECT i FROM index i WHERE i." + field + " = " + id;
        return session.createQuery(query, Index.class).getResultList();
    }
    
    
    public static void insertPage(Page page) {
//        Transaction transaction = session.beginTransaction();
        session.save(page);
//        transaction.commit();
    }
    
    public static void insertLemma(Lemma lemma) {
        Lemma foundLemma = getLemmaByLemma(lemma.getLemma());
        if (foundLemma == null) {
            lemma.setFrequency(1);
        } else {
            lemma = foundLemma;
            lemma.incrementFrequency();
        }
//        Transaction transaction = session.beginTransaction();
        session.save(lemma);
//        transaction.commit();
    }
    
    public static void insertLemmas(List<Lemma> lemmas) {
        lemmas.forEach(HibernateConnection::insertLemma);
    }
   
    public static void insertIndex(Index index) {
//        Transaction transaction = session.beginTransaction();
        session.save(index);
//        transaction.commit();
    }
    
    public static void insertIndexes(List<Index> indexes) {
        indexes.forEach(HibernateConnection::insertIndex);
    }
    
}
