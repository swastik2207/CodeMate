package com.example.user_service.dto;   


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequestDto {
    private String name;
    private String description;
    private String creatorId;
}
