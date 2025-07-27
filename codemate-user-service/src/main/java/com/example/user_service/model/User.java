package com.example.user_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.util.List;

@Document(collection = "users")  // Maps to the "users" collection in MongoDB
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;  // MongoDB automatically creates a unique _id index

    @Indexed(unique = true)
    private String username;

    private String password; 
    
    @Indexed(unique=true) // Should be stored hashed
    private String email;
    private int totalCodeExecutions;
    private List<CodeSubmission> submissions;

    // Add other fields like createdAt, roles, etc.
}
