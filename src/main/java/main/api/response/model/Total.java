package main.api.response.model;

import lombok.Getter;
import main.model.enums.Status;

import java.util.List;

@Getter
public class Total {
    
    private int sites;
    private int pages;
    private int lemmas;
    private boolean isIndexing;
    
    public Total(List<SiteInfo> detailed) {
        sites = detailed.size();
        pages = sumPages(detailed);
        lemmas = sumLemmas(detailed);
        isIndexing = isIndexing(detailed);
    }
    
    private int sumPages(List<SiteInfo> detailed) {
        return detailed.stream()
                .mapToInt(SiteInfo::getPages)
                .sum();
    }
    
    private int sumLemmas(List<SiteInfo> detailed) {
        return detailed.stream()
                .mapToInt(SiteInfo::getLemmas)
                .sum();
    }
    
    private boolean isIndexing(List<SiteInfo> detailed) {
        return detailed.stream()
                .noneMatch(i -> i.getStatus().equals(Status.INDEXING.toString()));
    }
}
