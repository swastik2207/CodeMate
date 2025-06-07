package com.example.user_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.util.List;

@Document(collection = "users")  // equivalent to @Entity + @Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;  // MongoDB uses String-based ObjectId by default

    private String username;
    private String password;  // should be hashed
    private String email;
    private int totalCodeExecutions;
    private List<CodeSubmission> submissions;

    // Add other fields like createdAt, roles, etc.
}
