package com.example.messagerie.service;

import com.example.messagerie.model.Notification;
import com.example.messagerie.model.NotificationType;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification notify(User user, NotificationType type, Long refId, String payload) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setReferenceId(refId);
        n.setPayload(payload);
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> listUnread(User user) {
        return notificationRepository.findByUserAndReadFlagOrderByCreatedAtDesc(user, false);
    }

    @Transactional
    public void markAllRead(User user) {
        List<Notification> list = notificationRepository.findByUserAndReadFlagOrderByCreatedAtDesc(user, false);
        list.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(list);
    }
}
