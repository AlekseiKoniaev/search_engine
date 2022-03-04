package main.repository.control;

import lombok.Getter;
import main.service.FieldService;
import main.service.IndexService;
import main.service.LemmaService;
import main.service.PageService;
import main.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class RepositoryControl {
    
    @Autowired
    private FieldService fieldService;
    
    @Autowired
    private PageService pageService;
    
    @Autowired
    private LemmaService lemmaService;
    
    @Autowired
    private IndexService indexService;
    
    @Autowired
    private SiteService siteService;
    
}
