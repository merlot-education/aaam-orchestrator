package eu.merloteducation.aaamorchestrator.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.views.Views;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin({
        "http://localhost:4200",
        "https://marketplace.dev.merlot-education.eu",
        "https://marketplace.demo.merlot-education.eu"
})
@RequestMapping("/")
public class UserQueryController {

    @Autowired
    private KeycloakRestService keycloakRestService;

    private final Logger logger = LoggerFactory.getLogger(UserQueryController.class);

    /**
     * GET request, for a given organization fetch the enrolled users if allowed to do so.
     *
     * @param orgaId id of organization to request the users from
     * @return List of user details of this organization
     */
    @GetMapping("/fromOrganization/{orgaId}")
    @JsonView(Views.UserDataView.class)
    public List<UserData> getFromOrganization(@PathVariable(value = "orgaId") String orgaId) {
        // get roles from the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // extract all orgaIds from the OrgRep and OrgLegRep Roles
        Set<String> orgaIds = roles
                .stream()
                .filter(s -> s.startsWith("ROLE_OrgRep_") || s.startsWith("ROLE_OrgLegRep_"))
                .map(s -> s.replace("ROLE_OrgRep_", "").replace("ROLE_OrgLegRep_", ""))
                .collect(Collectors.toSet());

        // if the requested organization id is not in the roles of this user,
        // the user is not allowed to request the list of users
        if (!orgaIds.contains(orgaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to request users for this organization.");
        }

        // otherwise the user may request the user list of this organization
        try {
            return keycloakRestService.getUsersInOrganization(orgaId);
        } catch (JsonProcessingException | JsonParseException | RestClientResponseException e) {
            logger.debug("Error during IAM communication", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with IAM backend.");
        }
    }


}
