/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.merloteducation.aaamorchestrator.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.views.Views;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserQueryController {
    private final KeycloakRestService keycloakRestService;

    public UserQueryController(@Autowired KeycloakRestService keycloakRestService) {
        this.keycloakRestService = keycloakRestService;
    }

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
            log.debug("Error during IAM communication", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with IAM backend.");
        }
    }


}
