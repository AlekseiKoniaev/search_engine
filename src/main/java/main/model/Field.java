package main.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    
    private int id;
    private String name;
    private String selector;
    private float weight;
    
    public Field(String name, String selector, float weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }
}
