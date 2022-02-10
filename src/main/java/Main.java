import parser.SiteWalker;
import repository.DBConnection;

import java.util.concurrent.ForkJoinPool;

public class Main {
    
    private static final String SITE_URL = "http://www.playback.ru/";
    
    public static void main(String[] args) {
    
        DBConnection.init();
        SiteWalker walker = new SiteWalker(SITE_URL);
        ForkJoinPool.commonPool().invoke(walker);
        DBConnection.close();
    }
}
