package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.user_service.dto.LoginRequest;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request.getUsername(), request.getPassword(), request.getEmail());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{username}")
    public ResponseEntity<LoginRequest> getUser(@PathVariable String username) {
      User user = userService.findByUsername(username);
    if (user != null) {
         LoginRequest loginRequest = new LoginRequest(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(loginRequest);
    } else {
        return ResponseEntity.notFound().build();
    }
    }

    @PostMapping("/{id}/increment-executions")
    public ResponseEntity<Void> incrementExecutions(@PathVariable String id) {
        userService.incrementCodeExecutions(id);
        return ResponseEntity.ok().build();
    }

    static class RegisterRequest {
        private String username;
        private String password;
        private String email;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
