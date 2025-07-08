package com.example.demo.feign;

import com.example.demo.dto.CodeSubmissionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.List;

@FeignClient(name = "code-execution-service", url = "http://localhost:8087")
public interface OnlineJudgeServiceClient {

    @PostMapping("/problems/solution/verify")
    ResponseEntity<List<String>> executeForProblem(@RequestBody CodeSubmissionRequest request);
}
