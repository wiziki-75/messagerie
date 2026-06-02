package com.example.messagerie.service;

import com.example.messagerie.model.*;
import com.example.messagerie.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final NotificationService notificationService;

    public MessageService(MessageRepository messageRepository, NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Message send(Conversation conv, User sender, String content, User recipient) {
        Message m = new Message();
        m.setConversation(conv);
        m.setSender(sender);
        m.setContent(content);
        Message saved = messageRepository.save(m);
        // admin log
        log.info("[ADMIN] Message envoyé: conv={}, from={}, contentLength={}", conv.getId(), sender.getId(), content == null ? 0 : content.length());
        // notification to recipient
        notificationService.notify(recipient, NotificationType.MESSAGE_RECEIVED, saved.getId(), "Nouveau message de " + sender.getName());
        return saved;
    }

    @Transactional
    public Message edit(Message message, String newContent, User recipient) {
        message.setContent(newContent);
        message.setEdited(true);
        message.setUpdatedAt(LocalDateTime.now());
        Message saved = messageRepository.save(message);
        notificationService.notify(recipient, NotificationType.MESSAGE_EDITED, saved.getId(), "Message édité");
        return saved;
    }

    @Transactional
    public Message softDelete(Message message, User recipient) {
        message.setDeleted(true);
        message.setUpdatedAt(LocalDateTime.now());
        Message saved = messageRepository.save(message);
        notificationService.notify(recipient, NotificationType.MESSAGE_DELETED, saved.getId(), "Message supprimé");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Message> list(Conversation conversation) {
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }
}
