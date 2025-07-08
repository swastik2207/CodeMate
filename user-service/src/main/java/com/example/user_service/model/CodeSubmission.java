package com.example.user_service.model;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSubmission {
    private String code;
    private String language;
    private List<List<String>> inputs;
    private Integer timeLimit;
    private Integer memoryLimit;
    private String submittedAt;  // optional timestamp
}
