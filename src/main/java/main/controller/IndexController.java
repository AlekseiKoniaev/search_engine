package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.response.IndexingResponse;
import main.api.response.StatResponse;
import main.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexController {
    
    @Autowired
    private final IndexingService indexingService;
    
    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() {
        return indexingService.startIndexing();
    }
    
    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        return indexingService.stopIndexing();
    }
    
    @PostMapping("/indexPage")
    public IndexingResponse indexPage(@RequestParam("url") String url) {
        IndexingResponse response = indexingService.indexPage(url);
        return response;
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<StatResponse> statistics() {
        StatResponse response = indexingService.statistics();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
