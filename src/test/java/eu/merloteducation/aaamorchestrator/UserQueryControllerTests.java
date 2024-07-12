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

import eu.merloteducation.aaamorchestrator.controller.UserQueryController;
import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.security.WebSecurityConfig;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import eu.merloteducation.authorizationlibrary.authorization.*;
import eu.merloteducation.authorizationlibrary.config.InterceptorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClientResponseException;


import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserQueryController.class, WebSecurityConfig.class})
@Import({ AuthorityChecker.class, ActiveRoleHeaderHandlerInterceptor.class, JwtAuthConverter.class, InterceptorConfig.class})
class UserQueryControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private KeycloakRestService keycloakRestService;

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    @MockBean
    private JwtAuthConverterProperties jwtAuthConverterProperties;

    @BeforeEach
    void setUp() throws Exception {
        List<UserData> udl = new ArrayList<>();
        UserData ud = new UserData();
        ud.setUsername("user");
        udl.add(ud);
        when(keycloakRestService.getUsersInOrganization(any())).thenReturn(udl);
        when(keycloakRestService.getUsersInOrganization("2")).thenThrow(RestClientResponseException.class);
    }

    @Test
    void getUsersUnautenticated() throws Exception
    {
        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsersAuthenticatedButUnauthorized() throws Exception
    {

        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/42")
                        .with(jwt().authorities(
                            new OrganizationRoleGrantedAuthority("OrgLegRep_10"),
                            new SimpleGrantedAuthority("ROLE_some_other_role")
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersAuthenticatedAndAuthorized() throws Exception
    {
        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/1")
                        .with(jwt().authorities(
                            new OrganizationRoleGrantedAuthority("OrgLegRep_1"),
                            new OrganizationRoleGrantedAuthority("OrgLegRep_2"),
                            new SimpleGrantedAuthority("ROLE_some_other_role")
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].username").value("user"));
    }

    @Test
    void getUsersAuthenticatedAuthorizedButBadBackendConnection() throws Exception
    {

        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/2")
                        .with(jwt().authorities(
                            new OrganizationRoleGrantedAuthority("OrgLegRep_1"),
                            new OrganizationRoleGrantedAuthority("OrgLegRep_2"),
                            new SimpleGrantedAuthority("ROLE_some_other_role")
                        ))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

}
