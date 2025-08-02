package com.example.user_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @NonNull
    private String password;

    @Indexed(unique = true)
    private String email;

    private int totalCodeExecutions;
    private List<CodeSubmission> submissions;
    private List<String> solvedProblems;
    private List<String> groupIds;
}
