package main;

import main.model.Finding;
import main.parser.SiteWalker;
import main.repository.DBConnection;
import main.searcher.Searcher;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class Main {
    
    private static final String SITE_URL = "http://www.playback.ru/";
    
    public static void main(String[] args) {
    
        search();
        
    }
    
    private static void indexSite() {
        DBConnection.init();
        SiteWalker walker = new SiteWalker(SITE_URL);
        ForkJoinPool.commonPool().invoke(walker);
        DBConnection.close();
    }
    
    private static void search() {
        DBConnection.connect();
        Searcher searcher = new Searcher();
        Set<Finding> result = searcher.search("купить новый смартфон");
        result.forEach(System.out::println);
        DBConnection.close();
    }
}
