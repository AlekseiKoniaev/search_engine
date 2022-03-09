package main.api.response;

import lombok.Getter;
import main.api.response.model.Finding;
import main.searcher.Searcher;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SearchResponse extends Response {
    
    private final int count;
    private final List<Finding> data;
    
    public SearchResponse(Searcher searcher) {
        super(true);
        count = searcher.getCount();
        data = new ArrayList<>(searcher.getSearchResult());
    }
}
