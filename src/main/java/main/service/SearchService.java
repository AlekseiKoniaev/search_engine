package main.service;

import main.api.request.model.SearchQuery;
import main.api.response.ErrorResponse;
import main.api.response.Response;
import main.api.response.SearchResponse;
import main.api.response.model.Finding;
import main.searcher.SearchQueryHandler;
import main.searcher.enums.SearchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    
    public static final String NOT_FOUND = "Ничего не найдено";
    public static final String WRONG_QUERY = "Не верный запрос";
    public static final String SERVER_ERROR = "Внутренняя ошибка сервера";
    
    private final ApplicationContext applicationContext;
    
    
    @Autowired
    public SearchService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    
    public ResponseEntity<Response> search(String query, String siteUrl, int offset, int limit) {
    
        SearchQuery searchQuery = new SearchQuery(query, siteUrl, offset, limit);
        SearchQueryHandler searchQueryHandler = applicationContext.getBean(SearchQueryHandler.class);
        searchQueryHandler.search(searchQuery);
        
        return getResponse(searchQueryHandler);
    }
    
    private ResponseEntity<Response> getResponse(SearchQueryHandler searchQueryHandler) {
        
        switch (searchQueryHandler.getStatus()) {
            case OK:
                int count = searchQueryHandler.getCount();
                List<Finding> findings = count == 0 ? null : searchQueryHandler.getFindings();
                return new ResponseEntity<>(new SearchResponse(count, findings), HttpStatus.OK);
            case NOT_FOUND:
                return new ResponseEntity<>(new ErrorResponse(NOT_FOUND), HttpStatus.OK);
            case WRONG_QUERY:
                return new ResponseEntity<>(new ErrorResponse(WRONG_QUERY), HttpStatus.OK);
            default:
                return new ResponseEntity<>(new ErrorResponse(SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
