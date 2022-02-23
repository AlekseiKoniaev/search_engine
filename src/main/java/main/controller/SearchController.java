package main.controller;

import lombok.RequiredArgsConstructor;
import main.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {
    
    @Autowired
    private final SearchService searchService;
    
}
