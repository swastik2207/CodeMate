package com.example.user_service.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
}
}
