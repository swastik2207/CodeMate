package com.example.demo.consumer;

import com.example.demo.dto.CodeSubmissionRequest;
import com.example.demo.dto.CodeExecutionResult;
import com.example.demo.service.CodeExecutionService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class CodeSubmissionConsumer {

 private static final Logger logger = LoggerFactory.getLogger(CodeSubmissionConsumer.class);

@Autowired
CodeExecutionService codeExecutionService;

@Autowired
KafkaTemplate<String,CodeExecutionResult>kafkaTemplate;

   @KafkaListener(
    topics = "code-execution-requests",
    groupId = "code-executor-group",
    containerFactory = "customKafkaListenerFactory"
)
public void consume(CodeSubmissionRequest request) {
    logger.info(" Received code submission request for execution. Request ID: {}", request.getRequestId());

    try {
        Map<String, List<String>> result = codeExecutionService.executeCode(request);

        List<String> output = result.get("output");
        logger.info(" Code execution completed. Output: {}", output);

        CodeExecutionResult executionResult = new CodeExecutionResult();
        executionResult.setRequestId(request.getRequestId());
        executionResult.setUserId(request.getUserId());
        executionResult.setOutput(output);
        executionResult.setStatus("SUCCESS");
        executionResult.setError(null);

        kafkaTemplate.send("code-execution-results", executionResult);
        logger.info(" Pushed execution result to 'code-execution-results' topic");

    } catch (Exception e) {
        CodeExecutionResult errorResult = new CodeExecutionResult();
        errorResult.setRequestId(request.getRequestId());
        errorResult.setUserId(request.getUserId());
        errorResult.setStatus("FAILED");
        errorResult.setError(e.getMessage());

        kafkaTemplate.send("code-execution-results", errorResult);
        logger.error(" Execution failed. Sent error result to Kafka. Error: {}", e.getMessage(), e);
    }
}

}
