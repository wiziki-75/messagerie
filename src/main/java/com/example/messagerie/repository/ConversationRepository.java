package com.example.messagerie.repository;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserAOrUserB(User a, User b);
    Optional<Conversation> findByUserAAndUserB(User a, User b);
}
