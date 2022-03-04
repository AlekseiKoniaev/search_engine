package main;

import main.api.response.model.Finding;
import main.repository.control.DBConnection;
import main.repository.control.HibernateConnection;
import main.searcher.Searcher;

import java.util.Set;

public class Main {
    
    private static final String SITE_URL = "http://www.playback.ru/";
    
    public static void main(String[] args) {
    
        test();
        
    }
    
    private static void test() {
        HibernateConnection.init();
        HibernateConnection.getPageCount();
    }
    
//    private static void indexSiteHibernate() {
//        HibernateConnection.init();
//        SiteWalker walker = new SiteWalker(SITE_URL);
//        ForkJoinPool.commonPool().invoke(walker);
//        HibernateConnection.close();
//    }
    
    private static void searchHibernate() {
        HibernateConnection.init();
        Searcher searcher = new Searcher();
        Set<Finding> result = searcher.search("купить новый смартфон");
        result.forEach(System.out::println);
        HibernateConnection.close();
    }
    
//    private static void indexSite() {
//        DBConnection.init();
//        SiteWalker walker = new SiteWalker(SITE_URL);
//        ForkJoinPool.commonPool().invoke(walker);
//        DBConnection.close();
//    }
    
    private static void search() {
        DBConnection.connect();
        Searcher searcher = new Searcher();
        Set<Finding> result = searcher.search("купить новый смартфон");
        result.forEach(System.out::println);
        DBConnection.close();
    }
}
