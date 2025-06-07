package com.example.demo.consumer;

import com.example.demo.dto.CodeSubmissionRequest;
import com.example.demo.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class CodeSubmissionConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CodeSubmissionConsumer.class);

    @Autowired
    private CodeExecutionService codeExecutionService;

    @KafkaListener(
        topics = "code-execution-requests",
        groupId = "code-executor-group",
        containerFactory = "customKafkaListenerFactory"
    )
    public void consume(CodeSubmissionRequest request) {
        logger.info(" Received code submission request from Kafka topic 'code-execution-requests'");
        logger.debug(" Code: {}, Language: {}", request.getCode(), request.getLanguage());

        try {
            Map<String, String> result = codeExecutionService.executeCode(request);

            logger.info(" Execution result:");
            result.forEach((key, value) -> logger.info("  {}: {}", key, value));
        } catch (Exception e) {
            logger.error(" Error executing code: {}", e.getMessage(), e);
        }
    }
}
