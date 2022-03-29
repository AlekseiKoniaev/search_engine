package main.walker;

import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.stream.Collectors;

public class PageParser {
    
    private final String siteUrl;
    private final Document document;
    
    public PageParser(Document document, String siteUrl) {
        this.siteUrl = siteUrl;
        this.document = document;
    }
    
    public Set<String> parseLink() {
        final String LINK_REGEX = "(" + siteUrl + ")?/[\\w/]+(\\.html|\\.php)?$";
        return document.select("a[href]")
                .stream()
                .map(e -> e.attr("href"))
                .filter(l -> (l.matches(LINK_REGEX)))
                .map(l -> l = l.replaceFirst(siteUrl, ""))
                .collect(Collectors.toSet());
    }
}
