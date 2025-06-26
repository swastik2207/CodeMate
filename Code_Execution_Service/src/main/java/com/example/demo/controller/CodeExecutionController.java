package com.example.demo.controller;

import com.example.demo.dto.CodeSubmissionRequest;
import com.example.demo.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/problems")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/solution/verify")
    public ResponseEntity<List<String>> executeForProblem(@RequestBody CodeSubmissionRequest request) {
        try {
            request.setRequestId(UUID.randomUUID().toString());
            Map<String, List<String>> result = codeExecutionService.executeCode(request);

           List<String> output =  result.get("output");

            if (output == null) {
                return ResponseEntity.status(500).body(List.of("Execution failed: " + result.getOrDefault("error", List.of("Unknown error"))));
            }

            
            return ResponseEntity.ok(output);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of("Unexpected Error: " + e.getMessage()));
        }
    }
}
