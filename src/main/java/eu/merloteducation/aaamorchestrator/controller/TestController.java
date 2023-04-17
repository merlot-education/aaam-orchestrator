package eu.merloteducation.aaamorchestrator.controller;

import eu.merloteducation.aaamorchestrator.entities.Greeting;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    @GetMapping("/anonymous")
    public Greeting getAnonymous() {
        return new Greeting(10, "content");
    }


    @GetMapping("/admin")
    public Greeting getAdmin(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("name");
        return new Greeting(20, userName);
    }

    @GetMapping("/missingrole")
    public Greeting getMissingRole(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        String userName = (String) token.getTokenAttributes().get("name");
        return new Greeting(30, userName);
    }

}