package com.example.authservice.controller;

import com.example.authservice.model.CodeExecutionRequest;
import com.example.authservice.service.KafkaProducerService;
import com.example.authservice.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/code")
public class CodeExecutionController {

    @Autowired
    private KafkaProducerService producerService;

    @Autowired
    private SseEmitterService emitterService;

    // Endpoint to trigger execution and return requestId
    @PostMapping("/execute")
    public String execute(@RequestBody CodeExecutionRequest request) {
        // 1. Generate unique requestId
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        // 2. Send to Kafka
        producerService.send(request);

        // 3. Return requestId so client can connect to /stream/{requestId}
        return requestId;
    }

    // Endpoint to register SSE emitter using requestId
    @GetMapping("/stream/{requestId}")
    public SseEmitter stream(@PathVariable String requestId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitterService.register(requestId, emitter);
        return emitter;
    }
}
