package main.model;

public class Index {
    
    private int id;
    private final int pageId;
    private final int lemmaId;
    private final float rank;
    
    public Index(int pageId, int lemmaId, float rank) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.rank = rank;
    }
    
    public int getPageId() {
        return pageId;
    }
    
    public int getLemmaId() {
        return lemmaId;
    }
    
    public float getRank() {
        return rank;
    }
}
