
package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeExecutionResult {
    private String requestId;
    private String userId;
    private String output;
    private String status;
    private String error;
}


