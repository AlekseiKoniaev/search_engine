package main.service;

import main.api.response.ErrorResponse;
import main.api.response.Response;
import main.api.response.SearchResponse;
import main.searcher.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    
    public static final String NOT_FOUND = "Совпадений не найдено";
    public static final String WRONG_QUERY = "Не верный запрос";
    public static final String SERVER_ERROR = "Внутренняя ошибка сервера";
    
    private final ApplicationContext applicationContext;
    
    
    @Autowired
    public SearchService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    
    public ResponseEntity<Response> search(String query, String siteUrl, int offset, int limit) {
    
        Searcher searcher = applicationContext.getBean(Searcher.class);
        searcher.search(query, siteUrl, offset, limit);
        
        return getResponse(searcher);
    }
    
    private ResponseEntity<Response> getResponse(Searcher searcher) {
        return switch (searcher.getStatus()) {
            case OK -> new ResponseEntity<>(new SearchResponse(searcher), HttpStatus.OK);
            case NOT_FOUND -> new ResponseEntity<>(new ErrorResponse(NOT_FOUND), HttpStatus.NOT_FOUND);
            case WRONG_QUERY -> new ResponseEntity<>(new ErrorResponse(WRONG_QUERY), HttpStatus.NOT_FOUND);
            case READY -> new ResponseEntity<>(new ErrorResponse(SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
