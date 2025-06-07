package com.example.authservice.service;

import com.example.authservice.model.CodeExecutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "code-execution-requests";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(CodeExecutionRequest request) {
        kafkaTemplate.send(TOPIC, request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error(" Failed to send request to Kafka for user {}: {}", request.getUserId(), ex.getMessage());
                    } else {
                        logger.info(" Request pushed to Kafka - User: {}, Topic: {}, Offset: {}",
                                request.getUserId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
