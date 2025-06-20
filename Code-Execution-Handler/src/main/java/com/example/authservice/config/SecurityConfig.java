package com.example.authservice.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // disable CSRF for testing (enable in prod with tokens)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/code/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(); // or use JWT/auth mechanism

        return http.build();
    }
}
