package main.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.model.enums.Status;
import org.hibernate.annotations.Type;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private int id;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Status status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    @Type(type = "org.hibernate.type.TextType")
    private String lastError;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;

    public Site(String url) {
        this.url = url;
        name = url;     // todo : get name from config
        status = Status.INDEXING;
        statusTime = LocalDateTime.now();
        lastError = null;   // todo : error processing
    }
}
