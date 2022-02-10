package parser;

import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.stream.Collectors;

public class PageParser {
    
    private final String rootPath;
    private final Document document;
    
    public PageParser(String rootPath, Document document) {
        this.rootPath = rootPath;
        this.document = document;
    }
    
    public Set<String> parseLink() {
        final String LINK_REGEX = "(" + rootPath + ")?/[\\w/]+(\\.html|\\.php)?$";
        return document.select("a[href]")
                .stream()
                .map(e -> e.attr("href"))
                .filter(l -> (l.matches(LINK_REGEX)))
                .map(l -> l = l.replaceFirst(rootPath, ""))
                .collect(Collectors.toSet());
    }
}
