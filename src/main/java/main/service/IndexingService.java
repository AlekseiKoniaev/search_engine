package main.service;

import main.api.response.IndexingResponse;
import main.api.response.StatResponse;

public interface IndexingService {
    
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexPage(String url);
    StatResponse statistics();
}
