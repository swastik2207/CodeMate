package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.model.CodeSubmission;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String username, String rawPassword, String email) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .totalCodeExecutions(0)
                .build();

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void incrementCodeExecutions(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTotalCodeExecutions(user.getTotalCodeExecutions() + 1);
            userRepository.save(user);
        });
    }

    public void saveSubmission(String userId, CodeSubmission submission) {
    userRepository.findById(userId).ifPresent(user -> {
        if (user.getSubmissions() == null) {
            user.setSubmissions(new ArrayList<>());
        }
        user.getSubmissions().add(submission);
        user.setTotalCodeExecutions(user.getTotalCodeExecutions() + 1);
        userRepository.save(user);
    });
}

}
