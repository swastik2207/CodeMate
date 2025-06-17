package com.example.authservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service // This ensures the service is a singleton bean
public class SseEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void register(String userId, SseEmitter emitter) {
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onCompletion(() -> emitters.remove(userId));
        emitters.put(userId, emitter);
    }

    public void sendToUser(String requestId, Map<String,String> data) {
    for (int i = 0; i < 10; i++) {
        SseEmitter emitter = emitters.get(requestId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("code-result").data(data));
                emitter.complete();
                emitters.remove(requestId);
            } catch (Exception e) {
                emitters.remove(requestId);
            }
            return;
        } else {
            try {
                Thread.sleep(2000); // wait 100ms and retry
            } catch (InterruptedException ignored) {}
        }
    }

    // If still not found, consider logging or queuing for later
    System.err.println("Emitter not registered in time for requestId: " + requestId);
}

}
