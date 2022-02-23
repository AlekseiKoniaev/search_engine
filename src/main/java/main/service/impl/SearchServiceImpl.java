package main.service.impl;

import lombok.RequiredArgsConstructor;
import main.model.Finding;
import main.repository.FieldRepository;
import main.repository.IndexRepository;
import main.repository.LemmaRepository;
import main.repository.PageRepository;
import main.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    
    @Override
    public List<Finding> search() {
        return null;
    }
}
