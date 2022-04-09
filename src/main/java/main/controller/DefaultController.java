package main.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

    @Value("${server.servlet.context-path}")
    private String webInterface;
    
    @Value("${app.backend-uri}")
    private String backendUri;
    
    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("webInterface", webInterface);
        model.addAttribute("backendUri", backendUri);
        return "index";
    }
}
