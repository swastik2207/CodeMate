package com.example.demo.feign;

import com.example.demo.dto.CodeSubmissionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "code-execution-service", url = "http://localhost:9090")
public interface OnlineJudgeServiceClient {

    @PostMapping("/problems/solution/verify")
    List<String> executeForProblem(@RequestBody CodeSubmissionRequest request);
}
