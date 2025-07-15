package poc.insurance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(config -> config.loginPage("/login"));
                // usa il default login page
        return http.build();
    }
}
