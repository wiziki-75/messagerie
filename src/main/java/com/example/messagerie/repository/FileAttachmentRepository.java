package com.example.messagerie.repository;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}
