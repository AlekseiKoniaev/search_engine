package main.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Lemma implements Comparable<Lemma> {
    
    private int id;
    private String lemma;
    private int frequency;
    private Integer siteId;
    
    
    public void incrementFrequency() {
        ++frequency;
    }
    
    public void decrementFrequency() {
        --frequency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Lemma lemma1 = (Lemma) o;
        
        if (!lemma.equals(lemma1.lemma)) return false;
        return siteId.equals(lemma1.siteId);
    }
    
    @Override
    public int hashCode() {
        int result = lemma.hashCode();
        result = 31 * result + siteId.hashCode();
        return result;
    }
    
    @Override
    public int compareTo(Lemma o) {
        int diff = siteId - o.siteId;
        if (diff != 0) {
            return diff;
        } else {
            return frequency != o.frequency ?
                    Integer.compare(frequency, o.frequency) : lemma.compareTo(o.lemma);
        }
    }
}
