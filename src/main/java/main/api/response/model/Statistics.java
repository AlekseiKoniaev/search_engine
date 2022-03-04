package main.api.response.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Statistics {
    private Total total;
    private List<SiteInfo> detailed;
    
    public Statistics(List<SiteInfo> detailed) {
        total = new Total(detailed);
        this.detailed = detailed;
    }
}
