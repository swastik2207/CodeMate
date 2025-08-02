package com.example.user_service.scheduler;

import com.example.user_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailScheduler {

    @Autowired
    private EmailService emailService;

    // Runs every day at 9:00 AM
    @Scheduled(cron = "0 0 0 * * ?")
    public void sendDailyReport() {
        emailService.sendEmail(
            "chatterjee.swastik022@gmail.com",
            "Daily Report",
            "This is your automated daily report."
        );
        System.out.println("Email sent at: " + java.time.LocalDateTime.now());
    }
}  