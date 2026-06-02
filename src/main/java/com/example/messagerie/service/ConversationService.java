package com.example.messagerie.service;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Conversation getOrCreate(User a, User b) {
        // enforce ordering to match unique constraint
        User first = a.getId() < b.getId() ? a : b;
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
    public List<Conversation> listFor(User user) {
        return conversationRepository.findByUserAOrUserB(user, user);
    }
}
