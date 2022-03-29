package main.controller;

import main.api.response.Response;
import main.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    
    private final SearchService searchService;
    
    
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    
    @GetMapping(value = "/search")
    public ResponseEntity<Response> search(
            @RequestParam("query") String query,
            @RequestParam(value = "site", required = false, defaultValue = "") String site,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
    
        return searchService.search(query, site, offset, limit);
    }
}
