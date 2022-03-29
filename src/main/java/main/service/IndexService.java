package main.service;

import main.model.Index;
import main.model.Page;
import main.repository.IndexRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {
    
    private final IndexRepository indexRepository;
    
    
    public IndexService(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }
    
    
    public void saveIndexes(List<Index> indexes) {
        indexRepository.saveAll(indexes);
    }
    
    public List<Index> findIndexesByPageId(int pageId) {
        return indexRepository.findByPageId(pageId);
    }
    
    public List<Index> findIndexesByLemmaId(int lemmaId) {
        return indexRepository.findByLemmaId(lemmaId);
    }
    
    public void deleteByPage(Page page) {
        indexRepository.deleteByPageId(page.getId());
    }
}
