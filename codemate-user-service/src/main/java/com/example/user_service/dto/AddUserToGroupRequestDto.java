package com.example.user_service.dto;

import lombok.Data;

@Data
public class AddUserToGroupRequestDto {
    private String groupCode;
    private String userId;
}
