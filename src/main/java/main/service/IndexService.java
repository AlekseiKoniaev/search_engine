package main.service;

import main.model.Index;
import main.model.Page;

import java.util.List;

public interface IndexService {
    
    void insertIndex(Index index);
    void saveIndexes(List<Index> indexes);
    List<Index> findIndexesByPageId(int pageId);
    List<Index> findIndexesByLemmaId(int lemmaId);
    void deleteByPages(List<Page> pages);
    void deleteByPage(Page page);
}
