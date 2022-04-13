package main.api.response;

import lombok.Getter;
import main.api.response.model.Finding;

import java.util.List;

@Getter
public class SearchResponse extends Response {
    
    private final int count;
    private final List<Finding> data;
    
    public SearchResponse(int count, List<Finding> findings) {
        super(true);
        this.count = count;
        data = findings;
    }
}
