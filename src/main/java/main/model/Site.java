package main.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.model.enums.Status;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class Site {

    private int id;
    private Status status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;

    public Site(String url) {
        this.url = url;
        status = Status.INDEXED;
        statusTime = LocalDateTime.now();
        lastError = null;   // todo : error processing
    }
    
    public synchronized void setStatus(Status status) {
        this.status = status;
        updateStatusTime();
    }
    
    public synchronized void updateStatusTime() {
        statusTime = LocalDateTime.now();
    }
}
