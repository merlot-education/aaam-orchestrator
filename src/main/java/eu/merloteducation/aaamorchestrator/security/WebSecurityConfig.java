package eu.merloteducation.aaamorchestrator.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    public static final String ADMIN = "OrgLegRep_1";  // TODO replace this with a general user role
    public static final String MISSING_ROLE = "missingrole";
    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/test/anonymous", "/test/anonymous/**", "/test/anonymous2", "/test/anonymous2/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/test/admin", "/test/admin/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/users", "/users/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/test/missingrole", "/test/missingrole/**").hasRole(MISSING_ROLE)
                .anyRequest().authenticated();
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
        return http.build();
    }
}