package com.example.user_service.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorizationDecision;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/posts/**").permitAll()
                .requestMatchers("/groups/**").permitAll()
                .requestMatchers("/user/vo").access((authentication, context) -> {
                    HttpServletRequest request = context.getRequest();
                    String sourceIp = request.getRemoteAddr();

                    // Allow only if the request comes from localhost
                    boolean isLocalhost = sourceIp.equals("127.0.0.1") || sourceIp.equals("0:0:0:0:0:0:0:1");
                    return new AuthorizationDecision(isLocalhost);
                })
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
