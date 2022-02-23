package main.service.impl;

import main.model.Page;
import main.repository.PageRepository;
import main.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;

public class PageServiceImpl implements PageService {
    
    @Autowired
    private PageRepository pageRepository;
    
    
    @Override
    public void insertPage(Page page) {
        pageRepository.save(page);
    }
    
    @Override
    public Page getPageById(int id) {
        return pageRepository.findById(id).orElse(null);
    }
    
    @Override
    public Page getPageByPath(String path) {
        return pageRepository.getPageByPath(path);
    }
    
    @Override
    public int getCount() {
        return (int) pageRepository.count();
    }
}
