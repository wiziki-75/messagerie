package com.example.messagerie.service;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.ConversationReadStatus;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.ConversationReadStatusRepository;
import com.example.messagerie.repository.ConversationRepository;
import com.example.messagerie.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationReadStatusRepository readStatusRepository,
                               MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.readStatusRepository = readStatusRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Conversation getOrCreate(User a, User b) {
        User first  = a.getId() < b.getId() ? a : b;
        User second = a.getId() < b.getId() ? b : a;
        Optional<Conversation> existing = conversationRepository.findByUserAAndUserB(first, second);
        return existing.orElseGet(() -> {
            Conversation c = new Conversation();
            c.setUserA(first);
            c.setUserB(second);
            return conversationRepository.save(c);
        });
    }

    @Transactional(readOnly = true)
    public Conversation require(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Conversation> listFor(User user) {
        return conversationRepository.findByUserAOrUserB(user, user);
    }

    @Transactional
    public void markRead(Conversation conv, User user) {
        ConversationReadStatus status = readStatusRepository
                .findByUserAndConversation(user, conv)
                .orElse(new ConversationReadStatus());
        status.setUser(user);
        status.setConversation(conv);
        status.setLastReadAt(LocalDateTime.now());
        readStatusRepository.save(status);
    }

    @Transactional(readOnly = true)
    public int unreadCount(Conversation conv, User currentUser) {
        LocalDateTime since = readStatusRepository
                .findByUserAndConversation(currentUser, conv)
                .map(ConversationReadStatus::getLastReadAt)
                .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));
        return messageRepository.countByConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(conv, currentUser, since);
    }
}
