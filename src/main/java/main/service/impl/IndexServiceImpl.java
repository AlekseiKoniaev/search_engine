package main.service.impl;

import main.model.Index;
import main.repository.IndexRepository;
import main.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IndexServiceImpl implements IndexService {
    
    @Autowired
    private IndexRepository indexRepository;
    
    @Override
    public void insertIndex(Index index) {
        indexRepository.save(index);
    }
    
    @Override
    public void insertIndexes(List<Index> indexes) {
        indexes.forEach(this::insertIndex);
    }
    
    @Override
    public List<Index> findIndexesByPageId(int pageId) {
        return indexRepository.findByPage(pageId);
    }
    
    @Override
    public List<Index> findIndexesByLemmaId(int lemmaId) {
        return indexRepository.findByLemma(lemmaId);
    }
}
