package main.service.impl;

import lombok.RequiredArgsConstructor;
import main.api.response.IndexingResponse;
import main.api.response.StatResponse;
import main.repository.FieldRepository;
import main.repository.IndexRepository;
import main.repository.LemmaRepository;
import main.repository.PageRepository;
import main.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    
    @Transactional
    @Override
    public IndexingResponse startIndexing() {
        return null;
    }
    
    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }
    
    @Override
    public IndexingResponse indexPage(String url) {
        return null;
    }
    
    @Override
    public StatResponse statistics() {
        return null;
    }
}
