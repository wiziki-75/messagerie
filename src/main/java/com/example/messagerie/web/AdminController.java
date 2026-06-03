package com.example.messagerie.web;

import com.example.messagerie.model.FileAttachment;
import com.example.messagerie.model.Message;
import com.example.messagerie.repository.FileAttachmentRepository;
import com.example.messagerie.repository.MessageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MessageRepository messageRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    public AdminController(MessageRepository messageRepository,
                           FileAttachmentRepository fileAttachmentRepository) {
        this.messageRepository = messageRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    // Story 15 — logs de tous les messages envoyés
    @GetMapping("/logs/messages")
    public List<Message> messageLogs() {
        return messageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // Story 16 — logs de tous les fichiers envoyés
    @GetMapping("/logs/files")
    public List<FileAttachment> fileLogs() {
        return fileAttachmentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
