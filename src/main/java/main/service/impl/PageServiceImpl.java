package main.service.impl;

import main.model.Page;
import main.model.Site;
import main.repository.PageRepository;
import main.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PageServiceImpl implements PageService {
    
    @Autowired
    private PageRepository pageRepository;
    
    
    @Override
    @Transactional
    public void savePage(Page page) {
        pageRepository.save(page);
    }
    
    @Override
    public Page getPageById(int id) {
        return pageRepository.findById(id).orElse(null);
    }
    
    @Override
    public Page getPageByPath(String path) {
        return pageRepository.findByPath(path);
    }
    
    @Override
    public Page getPageBySiteAndPath(Site site, String path) {
        return pageRepository.findBySiteIdAndPath(site.getId(), path);
    }
    
    @Override
    public List<Page> getPagesBySiteId(int siteId) {
        return pageRepository.findBySiteId(siteId);
    }
    
    @Override
    public int count() {
        return (int) pageRepository.count();
    }
    
    @Override
    public int countForSite(Site site) {
        return pageRepository.countForSite(site.getId());
    }
    
    @Override
    @Transactional
    public void deleteBySite(Site site) {
        pageRepository.deleteBySiteId(site.getId());
    }
    
    @Override
    @Transactional
    public void deleteBySiteAndPath(Site site, String path) {
        pageRepository.deleteBySiteIdAndPath(site.getId(), path);
    }
}
