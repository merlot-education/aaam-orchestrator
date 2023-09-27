package eu.merloteducation.aaamorchestrator.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.views.Views;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
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
    @PreAuthorize("@authorityChecker.representsOrganization(authentication, #orgaId)")
    public List<UserData> getFromOrganization(@PathVariable(value = "orgaId") String orgaId) {
        // the user may request the user list of this organization
        try {
            return keycloakRestService.getUsersInOrganization(orgaId);
        } catch (JsonProcessingException | JsonParseException | RestClientResponseException e) {
            logger.debug("Error during IAM communication", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with IAM backend.");
        }
    }


}
