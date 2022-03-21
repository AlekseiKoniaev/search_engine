package main.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DBConfig {
    
    private String url;
    private String username;
    private String password;
    
}
