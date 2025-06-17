
package com.example.authservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Getter
@Setter
@Data
public class CodeExecutionResult {
    private String requestId;
    private String userId;
    private String output;
    private String status;
    private String error;
}


