package com.example.messagerie.repository;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.Message;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    int countByConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(Conversation conv, User sender, LocalDateTime since);
}
