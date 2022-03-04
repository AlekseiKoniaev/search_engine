package main.service.impl;

import main.model.Site;
import main.repository.SiteRepository;
import main.service.SiteService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Service
public class SiteServiceImpl implements SiteService {
    
    @Autowired
    private SiteRepository siteRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    
    @Override
    @Transactional
    public void saveSite(Site site) {
        siteRepository.saveAndFlush(site);
    }
    
    @Override
    public Site getSiteById(int id) {
        return siteRepository.findById(id).orElse(null);
    }
    
    @Override
    public Site getSiteByUrl(String url) {
        return siteRepository.findByUrl(url);
    }
    
    @Override
    public List<Site> getAllSites() {
        Iterable<Site> siteIterable = siteRepository.findAll();
        List<Site> siteList = new ArrayList<>();
        for (Site site : siteIterable) {
            siteList.add(site);
        }
        return siteList;
    }
}
