package main.walker;

import main.model.Site;
import main.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import static main.model.enums.Status.INDEXED;

@Component
public class WalkerExecutor extends RecursiveAction {
    
    private final IndexingService indexingService;
    
    private List<SiteWalker> walkers;
    
    
    @Autowired
    public WalkerExecutor(IndexingService indexingService) {
        this.indexingService = indexingService;
    }
    
    public void init(List<SiteWalker> walkers) {
        this.walkers = walkers;
    }
    
    @Override
    public void compute() {
        
//        if (Thread.currentThread().isInterrupted()) {
//            return null;
//        }
        
        List<SiteWalker> walkerList = new ArrayList<>();
        walkers.forEach(walker -> {
            walker.fork();
            walkerList.add(walker);
        });
        walkerList.forEach(walker -> {
            walker.join();
            switchStatus(walker);
        });
        
    }
    
    public synchronized boolean stopIndexing(ForkJoinPool pool) {
    
        pool.shutdownNow();
        boolean successful = true;
        for (SiteWalker walker : walkers) {
            if (!switchStatus(walker)) {
                successful = false;
            }
        }
    
        return successful;
    }
    
    private boolean switchStatus(SiteWalker walker) {
    
        Site site = walker.getSite();
        site.setStatus(INDEXED);
        for (int i = 0; i < 10; i++) {
            indexingService.getSiteService().saveSite(site);
            if (indexingService.getSiteService().getSiteById(site.getId()).getStatus() == INDEXED) {
                return true;
            }
        }
        return false;
    }
}
