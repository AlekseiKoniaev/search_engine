package model;

public class Page implements Comparable<Page> {
    
    private String path;
    private int code;
    private String content;
    
    public Page(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    private void setPath(String path) {
        this.path = path;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public int compareTo(Page o) {
        return path.compareTo(o.path);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Page site = (Page) o;
    
        return path.equals(site.path);
    }
    
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    
}
