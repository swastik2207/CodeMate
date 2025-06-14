package com.example.auth_service.feign;

import com.example.auth_service.dto.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "http://localhost:8088") // or Eureka ID if using discovery
public interface UserServiceClient {

    @GetMapping("/user/{username}")
    LoginRequest getUserByUsername(@PathVariable("username") String username);
}
