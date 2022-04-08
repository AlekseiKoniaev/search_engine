package main.service;

import main.model.Page;
import main.model.Site;
import main.repository.PageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {
    
    private final PageRepository pageRepository;
    
    
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }
    
    public void savePage(Page page) {
        pageRepository.save(page);
    }
    
    public Page getPageById(int id) {
        return pageRepository.findById(id);
    }
    
    public Page getPageByPathAndSiteId(String path, int siteId) {
        return pageRepository.findByPathAndSiteId(path, siteId);
    }
    
    public List<Page> getPageBySiteId(int siteId) {
        return pageRepository.findBySiteId(siteId);
    }
    
    public int countBySite(Site site) {
        return site == null ? pageRepository.count() : pageRepository.countBySiteId(site.getId());
    }
    
    public void deleteBySiteId(int siteId) {
        pageRepository.deleteBySiteId(siteId);
    }
    
    public void deleteByPathAndSiteId(String path, int siteId) {
        pageRepository.deleteByPathAndSiteId(path, siteId);
    }
}
