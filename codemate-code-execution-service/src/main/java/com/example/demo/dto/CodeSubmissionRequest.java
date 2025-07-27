package com.example.demo.dto;

import lombok.Data;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CodeSubmissionRequest {
    private String code;
    private String language;
    private List<List<String>> inputs;
    private Integer timeLimit;
    private Integer memoryLimit;
    private String userId;
    private String requestId;
}
