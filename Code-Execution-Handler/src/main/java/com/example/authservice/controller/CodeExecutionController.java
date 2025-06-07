package com.example.authservice.controller;

import com.example.authservice.model.CodeExecutionRequest;
import com.example.authservice.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class CodeExecutionController {

    @Autowired
    private KafkaProducerService producerService;

    @PostMapping
    public String execute(@RequestBody CodeExecutionRequest request) {
        producerService.send(request);
        return "Execution request sent to Kafka";
    }
}
