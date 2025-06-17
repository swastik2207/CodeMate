package com.example.authservice.consumer;

import com.example.authservice.model.CodeExecutionResult;
import com.example.authservice.service.SseEmitterService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CodeExecutionResultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionResultConsumer.class);

    @Autowired
    private SseEmitterService emitterService;

    @KafkaListener(
        topics = "code-execution-results",
        groupId = "code-execution-result-group",
        containerFactory = "customKafkaListenerFactory"
    )
    public void consume(CodeExecutionResult result) {
        logger.info("Received Code Execution Result:");
        logger.info("Request ID: {}", result.getRequestId());
        logger.info("User ID: {}", result.getUserId());
        logger.info("Output: {}", result.getOutput());
        logger.info("Status: {}", result.getStatus());
        logger.info("Error: {}", result.getError());

        // Prepare data to send
        Map<String, String> data = new HashMap<>();
        data.put("output", result.getOutput());
        data.put("status", result.getStatus());
        data.put("error", result.getError());

        // Send result to the user via SSE using requestId
        emitterService.sendToUser(result.getRequestId(), data);
    }
}
