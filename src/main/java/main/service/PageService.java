package main.service;

import main.model.Page;
import main.model.Site;

import java.util.List;

public interface PageService {
    
    void savePage(Page page);
    Page getPageById(int id);
    Page getPageByPath(String path);
    Page getPageBySiteAndPath(Site site, String path);
    List<Page> getPagesBySiteId(int siteId);
    int count();
    int countForSite(Site site);
    void deleteBySite(Site site);
    void deleteBySiteAndPath(Site site, String path);
}
