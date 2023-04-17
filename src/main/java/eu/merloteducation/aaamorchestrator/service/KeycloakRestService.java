package eu.merloteducation.aaamorchestrator.service;

import eu.merloteducation.aaamorchestrator.entities.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KeycloakRestService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.available-roles-uri}")
    private String keycloakAvailableRoles;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.authorization-grant-type}")
    private String grantType;

    public String login(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username",username);
        map.add("password",password);
        map.add("client_id",clientId);
        map.add("grant_type",grantType);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, new HttpHeaders());
        return restTemplate.postForObject(keycloakTokenUri, request, String.class);
    }

    public String getAvailableRoles(String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", token);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return restTemplate.exchange(keycloakAvailableRoles, HttpMethod.GET, request, String.class).getBody();
    }

    public List<UserData> getUsersInOrganization(String token, String organizationId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        List<UserData> userData = new ArrayList<>();
        try {
            userData.addAll(restTemplate.exchange(
                    keycloakAvailableRoles + "/OrgLegRep_" + organizationId + "/users",
                    HttpMethod.GET, request, List.class).getBody());
        } catch (Exception e) {
            System.out.println("Skipping OrgLegRep for organization " + organizationId);
        }
        try {
            userData.addAll(restTemplate.exchange(
                    keycloakAvailableRoles + "/OrgRep_" + organizationId + "/users",
                    HttpMethod.GET, request, List.class).getBody());
        } catch (Exception e) {
            System.out.println("Skipping OrgRep for organization " + organizationId);
        }

        return userData;
    }

    public void logout(String refreshToken) throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id",clientId);
        map.add("refresh_token",refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        restTemplate.postForObject(keycloakLogout, request, String.class);
    }

}