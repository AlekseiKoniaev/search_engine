package main.service;

import main.model.Site;
import main.repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {
    
    private final SiteRepository siteRepository;
    
    
    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }
    
    
    public void saveSite(Site site) {
        siteRepository.save(site);
    }
    
    public void updateStatus(Site site) {
        siteRepository.updateStatus(site);
    }
    
    public void updateStatusTime(Site site) {
        siteRepository.updateStatusTime(site);
    }
    
    public Site getSiteById(int id) {
        return siteRepository.findById(id);
    }
    
    public Site getSiteByUrl(String url) {
        return siteRepository.findByUrl(url);
    }
    
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }
}
