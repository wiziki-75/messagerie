package com.example.messagerie.repository;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.ConversationReadStatus;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationReadStatusRepository extends JpaRepository<ConversationReadStatus, Long> {
    Optional<ConversationReadStatus> findByUserAndConversation(User user, Conversation conversation);
}
