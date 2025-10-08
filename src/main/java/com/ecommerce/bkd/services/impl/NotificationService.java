package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyUser(Long userId, String message) {
        // Implement notification logic (e.g., email, SMS, push notification)
        System.out.println("Notification to user " + userId + ": " + message);
    }

}
