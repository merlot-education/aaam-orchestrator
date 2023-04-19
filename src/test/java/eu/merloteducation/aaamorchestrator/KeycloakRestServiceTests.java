package eu.merloteducation.aaamorchestrator;

import eu.merloteducation.aaamorchestrator.entities.UserData;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
public class KeycloakRestServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.available-roles-uri}")
    private String keycloakAvailableRolesURI;
    @InjectMocks
    private KeycloakRestService keycloakRestService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(keycloakRestService, "keycloakTokenUri", keycloakTokenUri);
        ReflectionTestUtils.setField(keycloakRestService, "keycloakLogout", keycloakLogout);
        ReflectionTestUtils.setField(keycloakRestService, "keycloakAvailableRolesURI", keycloakAvailableRolesURI);
        when(restTemplate.postForObject(eq(keycloakTokenUri), any(), eq(String.class)))
                .thenReturn("{\"access_token\": \"1234\", \"refresh_token\": \"5678\"}");

        when(restTemplate.postForObject(eq(keycloakLogout), any(), eq(String.class)))
                .thenReturn("");

        // in general return empty list of users for any unspecified role endpoint
        when(restTemplate.exchange(startsWith(keycloakAvailableRolesURI), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        String mockUserResponse = """
        [{
            "id": "1234",
            "createdTimestamp": 1234,
            "username": "user",
            "enabled": true,
            "totp": true,
            "emailVerified": true,
            "firstName": "John",
            "lastName": "Doe",
            "email": "user@user.user",
            "attributes": null,
            "disableableCredentialTypes": [],
            "requiredActions": [],
            "notBefore": 1234
        }]
        """;
        // for endpoint with orga ID 1 return a single user
        when(restTemplate.exchange(eq(keycloakAvailableRolesURI + "/OrgLegRep_1/users"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockUserResponse, HttpStatus.OK));

    }

    @Test
    public void givenOrgaIdReturnUsers() throws Exception{

        // at endpoint 1 expect to get a list of UserData with a single user
        List<UserData> udl = keycloakRestService.getUsersInOrganization("1");
        assertThat(udl, isA(List.class));
        assertThat(udl, not(empty()));

        // at endpoint 42 expect to get no users
        udl = keycloakRestService.getUsersInOrganization("42");
        assertThat(udl, isA(List.class));
        assertThat(udl, empty());

    }

}
