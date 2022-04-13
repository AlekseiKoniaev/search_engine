package main.api.request.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SearchQuery {
    
    private final String query;
    private final String siteUrl;
    private final int offset;
    private final int limit;
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SearchQuery that = (SearchQuery) o;
        
        if (!query.equals(that.query)) return false;
        return siteUrl.equals(that.siteUrl);
    }
    
    @Override
    public int hashCode() {
        int result = query.hashCode();
        result = 31 * result + siteUrl.hashCode();
        return result;
    }
}
