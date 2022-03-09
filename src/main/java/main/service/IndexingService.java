package main.service;

import main.api.response.Response;
import main.api.response.StatResponse;

public interface IndexingService {
    
    Response startIndexing();
    Response stopIndexing();
    Response indexPage(String url);
    StatResponse statistics();
}
