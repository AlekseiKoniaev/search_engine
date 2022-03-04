package main.api.response;

import lombok.Getter;

@Getter
public class IndexingErrorResponse extends IndexingResponse {
    
    private final String error;
    
    public IndexingErrorResponse(String error) {
        super(false);
        this.error = error;
    }
    
    // todo : remove
    public IndexingErrorResponse(int count) {
        super(true);
        error = Integer.toString(count);
    }
}
