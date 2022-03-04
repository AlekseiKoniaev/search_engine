package main.service;

import main.api.response.model.Finding;

import java.util.List;

public interface SearchService {
    
    List<Finding> search();
}
