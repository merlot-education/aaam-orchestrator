package eu.merloteducation.aaamorchestrator.controller;

import eu.merloteducation.aaamorchestrator.entities.UserData;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UserQueryController {

    @Autowired
    private KeycloakRestService keycloakRestService;

    @GetMapping("/getmgmttoken")
    public String getAnonymous() {
        try {
            JsonParser parser = JsonParserFactory.getJsonParser();
            // TODO remove the hardcoded user/pass once they are actually used
            Map<String, Object> loginResult = parser.parseMap(keycloakRestService.login("usermgmt", "usermgmt"));
            String token = (String) loginResult.get("access_token");
            System.out.println(token);
            return "token generated";
        } catch (Exception e) {
            System.out.println(e);
            return "";
        }
    }

    @GetMapping("/getFromMyOrga/{orgaId}")
    public List<UserData> getAnonymous2(Principal principal, @PathVariable(value="orgaId") String orgaId) {
        // TODO verify that the user requesting this is actually in this organization (via the roles of the token)
        try {
            JsonParser parser = JsonParserFactory.getJsonParser();
            // TODO remove the hardcoded user/pass once they are actually used
            Map<String, Object> loginResult = parser.parseMap(keycloakRestService.login("usermgmt", "usermgmt"));
            String token = (String) loginResult.get("access_token");
            // TODO perhaps restrict the information about the users that is received here
            return keycloakRestService.getUsersInOrganization(token, "1");
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }


}
