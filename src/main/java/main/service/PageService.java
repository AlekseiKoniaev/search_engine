package main.service;

import main.model.Page;

public interface PageService {
    
    void insertPage(Page page);
//    void insertPages(List<Page> pages);
    Page getPageById(int id);
    Page getPageByPath(String path);
    int getCount();
    
}
