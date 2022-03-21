package main.controller;

import main.config.WebConfig;
import main.model.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DefaultController {

    @Autowired
    private WebConfig config;

    @RequestMapping("/admin")
    public List<Site> index() {
        return config.getSites();
    }

}
