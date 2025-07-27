package com.example.authservice.model;

import lombok.Data;
import java.util.List;

@Data
public class CodeExecutionRequest {
    private String code;
    private String language;
    private List<List<String>> inputs;
    private Integer timeLimit;
    private Integer memoryLimit;
    private String userId;// Injected from JWT auth
    private String requestId;  

    public void setRequestId( String requestId){
        this.requestId=requestId;
    }
}
