package main.api.response;

import lombok.Getter;
import main.api.response.model.SiteInfo;
import main.api.response.model.Statistics;

import java.util.List;

@Getter
public class StatResponse extends Response {
    
    private final Statistics statistics;
    
    public StatResponse(List<SiteInfo> detailed) {
        super(true);
        statistics = new Statistics(detailed);
    }
    
    
}
