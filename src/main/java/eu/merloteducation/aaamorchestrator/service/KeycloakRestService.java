/*
 *  Copyright 2023-2024 Dataport AöR
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

package eu.merloteducation.aaamorchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.aaamorchestrator.models.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class KeycloakRestService {

    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(KeycloakRestService.class);

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.available-roles-uri}")
    private String keycloakAvailableRolesURI;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.authorization-grant-type}")
    private String grantType;

    @Value("${keycloak.usermgmt-user}")
    private String keycloakUsermgmtUser;

    @Value("${keycloak.usermgmt-pass}")
    private String keycloakUsermgmtPass;

    private Map<String, Object> loginUsermgmt() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", keycloakUsermgmtUser);
        map.add("password", keycloakUsermgmtPass);
        map.add("client_id", clientId);
        map.add("grant_type", grantType);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, new HttpHeaders());
        String response = restTemplate.postForObject(keycloakTokenUri, request, String.class);

        JsonParser parser = JsonParserFactory.getJsonParser();
        return parser.parseMap(response);
    }

    private void logoutUsermgmt(String refreshToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        restTemplate.postForObject(keycloakLogout, request, String.class);
    }

    /**
     * For a given organization id, request all enrolled users from the authentication backend.
     *
     * @param organizationId id to request user list for
     * @return list of users in organization
     */
    public List<UserData> getUsersInOrganization(String organizationId) throws RestClientResponseException,
            JsonProcessingException, JsonParseException {

        // prepare OAuth2 session as usermgmt account for reading users in keycloak
        // note this is separate from the token that was provided by the user asking for this execution
        Map<String, Object> usermgmtLoginResponse;
        usermgmtLoginResponse = loginUsermgmt();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + usermgmtLoginResponse.get("access_token"));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);

        // ask the respective endpoints for the users enrolled in the specified organization ID
        ObjectMapper mapper = new ObjectMapper();
        List<UserData> userData = new ArrayList<>();
        String[] endpoints = {"OrgLegRep", "FedAdmin"};

        for (String ep : endpoints) {
            String epStr = keycloakAvailableRolesURI + "/" + ep + "_" + organizationId + "/users";
            try {
                URI epUri = new URI(epStr.replace("#", "%23"));
                // get the users in this role, this will return a list of jsons
                String response = restTemplate.exchange(epUri, HttpMethod.GET, request, String.class).getBody();
                // take the response (if it failed we get into the catch) and map it to the entity class
                List<UserData> ud = mapper.readValue(response,
                        mapper.getTypeFactory().constructCollectionType(List.class, UserData.class));
                ud = ud.stream().filter(UserData::isEnabled).toList();
                ud.forEach(u -> u.setOrgaRole(ep));
                userData.addAll(ud);
            } catch (RestClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.info("No data at endpoint: {}", epStr);
                } else {
                    throw e;
                }
            } catch (URISyntaxException e) {
                logger.info("Failed to build URI. {}", e.getMessage());
            }
        }

        // end user management session
        this.logoutUsermgmt((String) usermgmtLoginResponse.get("refresh_token"));

        return userData;
    }

}