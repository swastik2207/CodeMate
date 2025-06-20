package com.example.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.auth_service.config.JwtConfig;



@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
@EnableFeignClients(basePackages = "com.example.auth_service.feign") 
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
