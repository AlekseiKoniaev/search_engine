package main.walker;

import main.model.Site;
import main.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static main.model.enums.Status.INDEXED;

@Component
public class WalkerExecutor extends RecursiveAction {
    
    private final SiteService siteService;
    
    private List<SiteWalker> walkers;
    private ForkJoinPool executorPool;
    
    
    @Autowired
    public WalkerExecutor(SiteService siteService) {
        this.siteService = siteService;
    }
    
    
    public void init(List<SiteWalker> walkers, ForkJoinPool pool) {
        this.walkers = walkers;
        this.executorPool = pool;
    }
    
    boolean isDisableCompute() {
        return executorPool.isTerminating() || executorPool.isTerminated();
    }
    
    @Override
    public void compute() {
        
        if (isDisableCompute()) {
            return;
        }
        
        List<SiteWalker> walkerList = new ArrayList<>();
        
        for (SiteWalker walker : walkers) {
            walker.setExecutor(this);
            walker.fork();
            walkerList.add(walker);
        }
        
        for (SiteWalker walker : walkerList) {
            walker.join();
            switchStatus(walker);
        }
    
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
        
        siteService.updateStatus(site);
        return siteService.getSiteById(site.getId()).getStatus() == INDEXED;
    }
}
