package main.service;

import main.model.Site;

import java.util.List;

public interface SiteService {
    
    void saveSite(Site site);
    Site getSiteById(int id);
    Site getSiteByUrl(String url);
    List<Site> getAllSites();
}
