package com.example.messagerie.web;

import com.example.messagerie.model.Notification;
import com.example.messagerie.model.User;
import com.example.messagerie.service.NotificationService;
import com.example.messagerie.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/unread/{userId}")
    public List<Notification> unread(@PathVariable Long userId) {
        User user = userService.require(userId);
        return notificationService.listUnread(user);
    }

    @PostMapping("/mark-all-read/{userId}")
    public void markAllRead(@PathVariable Long userId) {
        User user = userService.require(userId);
        notificationService.markAllRead(user);
    }
}
