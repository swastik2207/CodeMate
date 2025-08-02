package com.example.user_service.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.time.LocalDateTime;
import java.util.*;

@Document(collection = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    private String id;

    private String name;
    private String description;
    private String creatorId;
    private String groupCode;

    private List<String> memberIds;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
private LocalDateTime updatedAt;
}
