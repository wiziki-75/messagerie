package com.example.messagerie.repository;

import com.example.messagerie.model.Notification;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFlagOrderByCreatedAtDesc(User user, boolean readFlag);
}
