package com.example.messagerie.repository;

import com.example.messagerie.model.GroupConversation;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupConversationRepository extends JpaRepository<GroupConversation, Long> {
    @Query("SELECT g FROM GroupConversation g JOIN g.members m WHERE m = :user")
    List<GroupConversation> findByMember(@Param("user") User user);
}
