package com.example.messagerie.repository;

import com.example.messagerie.model.GroupConversation;
import com.example.messagerie.model.GroupMessage;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupConversationOrderByCreatedAtAsc(GroupConversation group);
    int countByGroupConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(GroupConversation group, User sender, LocalDateTime since);
}
