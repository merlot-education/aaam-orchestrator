package eu.merloteducation.aaamorchestrator.controller;

import com.fasterxml.jackson.annotation.JsonView;
import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.views.Views;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/")
public class UserQueryController {

    @Autowired
    private KeycloakRestService keycloakRestService;

    @GetMapping("health")
    public void getHealth() {
    }


    @GetMapping("/fromOrganization/{orgaId}")
    @JsonView(Views.UserDataView.class)
    public List<UserData> getFromOrganization(Principal principal,
                                        @PathVariable(value="orgaId") String orgaId,
                                        HttpServletResponse response) {
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
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        // otherwise the user may request the user list of this organization
        try {
            return keycloakRestService.getUsersInOrganization(orgaId);
        } catch (Exception e) {
            System.out.println(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

    }


}
