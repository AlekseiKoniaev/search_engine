package main.walker;

import main.model.Site;
import main.service.impl.IndexingServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static main.model.enums.Status.INDEXED;

public class WalkerExecutor extends RecursiveTask<Void> {
    
    private final List<SiteWalker> walkers;
    private final IndexingServiceImpl indexingService;
    
    public WalkerExecutor(List<SiteWalker> walkers, IndexingServiceImpl indexingService) {
        this.walkers = walkers;
        this.indexingService = indexingService;
    }
    
    @Override
    public Void compute() {
        
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        
        List<SiteWalker> walkerList = new ArrayList<>();
        walkers.forEach(walker -> {
            walker.fork();
            walkerList.add(walker);
        });
        walkerList.forEach(walker -> {
            walker.join();
            switchStatus(walker);
        });
        
        return null;
    }
    
    // todo : remake to boolean
    public synchronized int stopIndexing(ForkJoinPool pool) {
    
        pool.shutdownNow();
        int count = 0;
        for (SiteWalker walker : walkers) {
            count += switchStatus(walker);
        }
    
        return count;
    }
    
    @Transactional
    private int switchStatus(SiteWalker walker) {
    
        Site site = walker.getPage().getSite();
        site.setStatus(INDEXED);
        int count = 0;
        for (int i = 0; i < 10; i++) {
            indexingService.getSiteService().saveSite(site);
            if (indexingService.getSiteService().getSiteById(site.getId()).getStatus() == INDEXED) {
                count = i + 1;
                break;
            }
        }
        return count;
    }
}
