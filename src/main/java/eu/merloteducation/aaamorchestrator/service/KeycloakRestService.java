package eu.merloteducation.aaamorchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.aaamorchestrator.entities.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
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
        map.add("username",keycloakUsermgmtUser);
        map.add("password",keycloakUsermgmtPass);
        map.add("client_id",clientId);
        map.add("grant_type",grantType);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, new HttpHeaders());
        String response = restTemplate.postForObject(keycloakTokenUri, request, String.class);

        JsonParser parser = JsonParserFactory.getJsonParser();
        Map<String, Object> loginResult = parser.parseMap(response);
        return loginResult;
    }

    private void logoutUsermgmt(String refreshToken) throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        String result = restTemplate.postForObject(keycloakLogout, request, String.class);
    }

    public String getAvailableRoles(String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", token);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return restTemplate.exchange(keycloakAvailableRolesURI, HttpMethod.GET, request, String.class).getBody();
    }

    public List<UserData> getUsersInOrganization(String organizationId) throws Exception {

        // prepare OAuth2 session as usermgmt account for reading users in keycloak
        // note this is separate from the token that was provided by the user asking for this execution
        Map<String, Object> usermgmtLoginResponse = loginUsermgmt();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + usermgmtLoginResponse.get("access_token"));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);

        // ask the respective endpoints for the users enrolled in the specified organization ID
        ObjectMapper mapper = new ObjectMapper();
        List<UserData> userData = new ArrayList<>();
        String[] endpoints = {"OrgLegRep", "OrgRep"};

        for (String ep : endpoints) {
            String ep_uri = keycloakAvailableRolesURI + "/" + ep + "_" + organizationId + "/users";
            try {
                // get the users in this role, this will return a list of jsons
                String response = restTemplate.exchange(ep_uri, HttpMethod.GET, request, String.class).getBody();
                // take the response (if it failed we get into the catch) and map it to the entity class
                List<UserData> ud = mapper.readValue(response,
                        mapper.getTypeFactory().constructCollectionType(List.class, UserData.class));
                ud.forEach(u -> u.setOrgaRole(ep));
                userData.addAll(ud);
            } catch (Exception e) {
                System.out.println(ep_uri + " : " + e);
            }
        }

        // end user management session
        this.logoutUsermgmt((String) usermgmtLoginResponse.get("refresh_token"));

        return userData;
    }

}