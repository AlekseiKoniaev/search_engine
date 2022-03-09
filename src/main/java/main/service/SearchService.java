package main.service;

import main.api.response.Response;
import org.springframework.http.ResponseEntity;

public interface SearchService {
    
    ResponseEntity<Response> search(String query, String site, int offset, int limit);
}
