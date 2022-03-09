package main.controller;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class IndexController {
    
    @Autowired
    private final IndexingService indexingService;
    
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
