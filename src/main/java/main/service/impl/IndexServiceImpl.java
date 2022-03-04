package main.service.impl;

import main.model.Index;
import main.model.Page;
import main.model.Site;
import main.repository.IndexRepository;
import main.repository.PageRepository;
import main.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {
    
    @Autowired
    private PageRepository pageRepository;
    
    @Autowired
    private IndexRepository indexRepository;
    
    @Override
    public void insertIndex(Index index) {
        indexRepository.save(index);
    }
    
    @Override
    public void saveIndexes(List<Index> indexes) {
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
    
    @Override
    public void deleteByPages(List<Page> pages) {
        pages.forEach(this::deleteByPage);
    }
    
    @Override
    @Transactional
    public void deleteByPage(Page page) {
        indexRepository.deleteByPageId(page.getId());
    }
}
