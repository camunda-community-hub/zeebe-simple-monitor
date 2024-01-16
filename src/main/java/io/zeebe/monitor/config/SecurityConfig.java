package io.zeebe.monitor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(
prefix = "spring",
name = "security.enabled",
havingValue = "true")
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  LogoutHandler logoutHandler(final RestTemplate restTemplate) {
    return new KeycloakLogoutHandler(restTemplate);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, LogoutHandler logoutHandler) throws Exception {
    http
    .authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/actuator/health/**").permitAll()
    .anyRequest().authenticated()
    )
    .oauth2Login(Customizer.withDefaults())
    .logout(oauth2 -> oauth2.addLogoutHandler(logoutHandler).logoutSuccessUrl("/"));
    return http.build();
  }
}