package main.controller;

import main.api.response.Response;
import main.api.response.StatResponse;
import main.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    
    private final IndexingService indexingService;
    
    
    @Autowired
    public IndexController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }
    
    
    @GetMapping("/startIndexing")
    public Response startIndexing() {
        return indexingService.startIndexing();
    }
    
    @GetMapping("/stopIndexing")
    public Response stopIndexing() {
        return indexingService.stopIndexing();
    }
    
    @PostMapping("/indexPage")
    public Response indexPage(@RequestParam("url") String url) {
        return indexingService.indexPage(url);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<StatResponse> statistics() {
        StatResponse response = indexingService.statistics();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
