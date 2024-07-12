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

package eu.merloteducation.aaamorchestrator;

import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
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
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeycloakRestServiceTests {

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
    void beforeAll() throws URISyntaxException {
        ReflectionTestUtils.setField(keycloakRestService, "keycloakTokenUri", keycloakTokenUri);
        ReflectionTestUtils.setField(keycloakRestService, "keycloakLogout", keycloakLogout);
        ReflectionTestUtils.setField(keycloakRestService, "keycloakAvailableRolesURI", keycloakAvailableRolesURI);
        ReflectionTestUtils.setField(keycloakRestService, "restTemplate", restTemplate);
        when(restTemplate.postForObject(eq(keycloakTokenUri), any(), eq(String.class)))
                .thenReturn("{\"access_token\": \"1234\", \"refresh_token\": \"5678\"}");

        when(restTemplate.postForObject(eq(keycloakLogout), any(), eq(String.class)))
                .thenReturn("");

        lenient().when(restTemplate.exchange(eq(new URI(keycloakAvailableRolesURI + "/OrgLegRep_99/users")), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RestClientResponseException("not found", HttpStatus.NOT_FOUND, "not found", null, null, null));
        lenient().when(restTemplate.exchange(eq(new URI(keycloakAvailableRolesURI + "/FedAdmin_99/users")), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RestClientResponseException("not found", HttpStatus.NOT_FOUND, "not found", null, null, null));

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
        lenient().when(restTemplate.exchange(eq(new URI(keycloakAvailableRolesURI + "/OrgLegRep_1/users")), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockUserResponse, HttpStatus.OK));
        lenient().when(restTemplate.exchange(eq(new URI(keycloakAvailableRolesURI + "/FedAdmin_1/users")), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RestClientResponseException("not found", HttpStatus.NOT_FOUND, "not found", null, null, null));

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
        List<UserData> udl = keycloakRestService.getUsersInOrganization("99");
        assertThat(udl, isA(List.class));
        assertTrue(udl.isEmpty());

    }
}
