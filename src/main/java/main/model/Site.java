package main.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.model.enums.Status;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private volatile Status status;

    @Setter(AccessLevel.NONE)
    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Setter(AccessLevel.NONE)
    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;

    public Site(String url) {
        this.url = url;
        status = Status.NOT_INDEXED;
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
