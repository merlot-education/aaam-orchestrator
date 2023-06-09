package eu.merloteducation.aaamorchestrator;

import eu.merloteducation.aaamorchestrator.controller.UserQueryController;
import eu.merloteducation.aaamorchestrator.models.UserData;
import eu.merloteducation.aaamorchestrator.security.JwtAuthConverter;
import eu.merloteducation.aaamorchestrator.service.KeycloakRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserQueryController.class)
public class UserQueryControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private KeycloakRestService keycloakRestService;

    @MockBean
    private JwtAuthConverter jwtAuthConverter;

    @BeforeEach
    public void setUp() throws Exception {
        List<UserData> udl = new ArrayList<>();
        UserData ud = new UserData();
        ud.setUsername("user");
        udl.add(ud);
        when(keycloakRestService.getUsersInOrganization(any())).thenReturn(udl);
    }

    @Test
    public void getUsersUnautenticated() throws Exception
    {
        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getUsersAuthenticatedButUnauthorized() throws Exception
    {

        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/42")
                        .with(user("user").password("user").roles("USER","ADMIN", "OrgLegRep_1"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUsersAuthenticatedAndAuthorized() throws Exception
    {
        mvc.perform(MockMvcRequestBuilders
                        .get("/fromOrganization/1")
                        .with(user("user").password("user").roles("USER","ADMIN", "OrgLegRep_1"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].username").value("user"));
    }

}
