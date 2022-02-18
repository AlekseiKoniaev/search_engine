package model;

public class Lemma implements Comparable<Lemma> {
    
    private int id;
    private String lemma;
    private int frequency;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getLemma() {
        return lemma;
    }
    
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    @Override
    public int compareTo(Lemma o) {
        return frequency != o.frequency ?
                Integer.compare(frequency, o.frequency) : lemma.compareTo(o.lemma);
    }
}
