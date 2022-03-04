package main.api.response;

import lombok.Getter;
import main.api.response.model.SiteInfo;
import main.api.response.model.Statistics;

import java.util.List;

@Getter
public class StatResponse {
    
    private final boolean result;
    private final Statistics statistics;
    
    public StatResponse(List<SiteInfo> detailed) {
        result = true;
        statistics = new Statistics(detailed);
    }
    
    
}
