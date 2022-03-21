package main.config;

import lombok.Getter;
import lombok.Setter;
import main.model.Site;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@Configuration
@ConfigurationProperties(prefix = "app")
public class WebConfig {
    
    private List<Site> sites;
    private String userAgent;
    private String referrer;
    private String webInterface;
    
}
