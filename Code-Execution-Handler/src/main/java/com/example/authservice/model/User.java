package com.example.authservice.model;

import lombok.Data;

@Data
public class User {
    private String id;
    private String username;
    private String password;
    private String token; // JWT Token, stored after login or issued at login
}
