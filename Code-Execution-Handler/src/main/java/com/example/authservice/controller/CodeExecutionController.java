package com.example.authservice.controller;

import com.example.authservice.model.CodeExecutionRequest;
import com.example.authservice.service.KafkaProducerService;
import com.example.authservice.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.*;

@RestController
@RequestMapping("/code")
public class CodeExecutionController {

    @Autowired
    private KafkaProducerService producerService;

    @Autowired
    private SseEmitterService emitterService;

    // Endpoint to trigger execution and return requestId
    
 @PostMapping("/execute")
public ResponseEntity<Map<String, String>> execute(@RequestBody CodeExecutionRequest request) {
    // 1. Generate unique requestId
    String requestId = UUID.randomUUID().toString();
    request.setRequestId(requestId);

    // 2. Send to Kafka
    producerService.send(request);

    // 3. Create response map
    Map<String, String> response = new HashMap<>();
    response.put("requestId", requestId);

    // 4. Return wrapped in ResponseEntity with 200 OK
    return ResponseEntity.ok(response);
}


    // Endpoint to register SSE emitter using requestId
    @GetMapping("/stream/{requestId}")
    public SseEmitter stream(@PathVariable String requestId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitterService.register(requestId, emitter);
        return emitter;
    }
}
