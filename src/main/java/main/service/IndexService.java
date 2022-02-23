package main.service;

import main.model.Index;

import java.util.List;

public interface IndexService {
    
    void insertIndex(Index index);
    void insertIndexes(List<Index> indexes);
    List<Index> findIndexesByPageId(int pageId);
    List<Index> findIndexesByLemmaId(int lemmaId);
    
}
