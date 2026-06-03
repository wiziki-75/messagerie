package com.example.messagerie.repository;

import com.example.messagerie.model.GroupConversation;
import com.example.messagerie.model.GroupConversationReadStatus;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupConversationReadStatusRepository extends JpaRepository<GroupConversationReadStatus, Long> {
    Optional<GroupConversationReadStatus> findByUserAndGroupConversation(User user, GroupConversation group);
}
