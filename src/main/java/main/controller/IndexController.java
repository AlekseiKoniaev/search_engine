package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.response.IndexingResponse;
import main.api.response.StatResponse;
import main.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexController {
    
    @Autowired
    private final IndexingService indexingService;
    
    @GetMapping("/api/startIndexing")
    public IndexingResponse startIndexing() {
        return indexingService.startIndexing();
    }
    
    @GetMapping("/api/stopIndexing")
    public IndexingResponse stopIndexing() {
        return indexingService.stopIndexing();
    }
    
    @PostMapping("/api/indexPage/{url}")
    public IndexingResponse indexPage(@PathVariable String url) {
        return indexingService.indexPage(url);
    }
    
    @GetMapping("api/statistics")
    public StatResponse statistics() {
        return indexingService.statistics();
    }
}
