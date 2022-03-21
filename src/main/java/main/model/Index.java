package main.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Index {
    
    private int id;
    private Integer pageId;
    private Integer lemmaId;
    private float rank;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Index index = (Index) o;
        
        if (!pageId.equals(index.pageId)) return false;
        return lemmaId.equals(index.lemmaId);
    }
    
    @Override
    public int hashCode() {
        int result = pageId.hashCode();
        result = 31 * result + lemmaId.hashCode();
        return result;
    }
}
