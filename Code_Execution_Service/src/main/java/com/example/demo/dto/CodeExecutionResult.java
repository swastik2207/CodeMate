
package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter
@Setter
public class CodeExecutionResult {
    private String requestId;
    private String userId;
    private List<String> output;
    private String status;
    private String error;
}
