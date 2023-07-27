package eu.merloteducation.aaamorchestrator;

import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeycloakRestServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.available-roles-uri}")
    private String keycloakAvailableRolesURI;
    @Autowired
    private KeycloakRestService keycloakRestService;

    @BeforeAll
    public void beforeAll() {
        ReflectionTestUtils.setField(keycloakRestService, "keycloakTokenUri", keycloakTokenUri);
        ReflectionTestUtils.setField(keycloakRestService, "keycloakLogout", keycloakLogout);
        ReflectionTestUtils.setField(keycloakRestService, "keycloakAvailableRolesURI", keycloakAvailableRolesURI);
        ReflectionTestUtils.setField(keycloakRestService, "restTemplate", restTemplate);
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
    void givenOrgaIdReturnUsersValid() throws Exception{

        // at endpoint 1 expect to get a list of UserData with a single user
        List<UserData> udl = keycloakRestService.getUsersInOrganization("1");
        assertThat(udl, isA(List.class));
        assertFalse(udl.isEmpty());
        assertEquals(1, udl.size());
        UserData entry = udl.get(0);
        assertEquals("1234", entry.getId());
        assertEquals(1234L, entry.getCreatedTimestamp());
        assertEquals("user", entry.getUsername());
        assertTrue(entry.isEnabled());
        assertTrue(entry.isTotp());
        assertTrue(entry.isEmailVerified());
        assertEquals("John", entry.getFirstName());
        assertEquals("Doe", entry.getLastName());
        assertEquals("user@user.user", entry.getEmail());
        assertNull(entry.getAttributes());
        assertTrue(entry.getDisableableCredentialTypes().isEmpty());
        assertTrue(entry.getRequiredActions().isEmpty());
        assertEquals(1234L, entry.getNotBefore());

    }

    @Test
    void givenOrgaIdReturnUsersNonExistent() throws Exception{

        // at endpoint 42 expect to get no users
        List<UserData> udl = keycloakRestService.getUsersInOrganization("42");
        assertThat(udl, isA(List.class));
        assertTrue(udl.isEmpty());

    }

}
