package main.api.response.model;

import lombok.Getter;
import lombok.Setter;
import main.model.Page;

@Getter
@Setter
public class PreFinding extends Finding {
    
    private Page page;
}
