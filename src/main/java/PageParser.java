import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.stream.Collectors;

public class PageParser {
    
    private final String rootPath;
    private final Document content;
    private Set<String> linkMap;
    
    public PageParser(String rootPath, Document content) {
        this.rootPath = rootPath;
        this.content = content;
        parse();
    }
    
    private void parse() {
        final String ROOT_REGEX = "(" + rootPath + ")?";
        final String LINK_REGEX = "/[\\w/]+(\\.html|\\.php)?$";
        linkMap = content.select("a[href]")
                .stream()
                .map(e -> e.attr("href"))
                .filter(l -> (l.matches(ROOT_REGEX + LINK_REGEX)))
                .map(l -> l = l.replaceFirst(rootPath, ""))
                .collect(Collectors.toSet());
    }
    
    public Set<String> getParsedPaths() {
        return linkMap;
    }
}
