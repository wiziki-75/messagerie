package com.example.messagerie.web;

import com.example.messagerie.model.*;
import com.example.messagerie.repository.ConversationRepository;
import com.example.messagerie.repository.MessageRepository;
import com.example.messagerie.service.MessageService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    public MessageController(MessageService messageService,
                             ConversationRepository conversationRepository,
                             MessageRepository messageRepository,
                             UserService userService) {
        this.messageService = messageService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @PostMapping("/send")
    public Message send(@RequestBody @Valid Dtos.SendMessageRequest req) {
        Conversation conv = conversationRepository.findById(req.conversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));
        User sender = userService.require(req.senderId());
        Long aId = conv.getUserA().getId();
        Long bId = conv.getUserB().getId();
        if (!req.senderId().equals(aId) && !req.senderId().equals(bId))
            throw new IllegalArgumentException("L'expéditeur n'appartient pas à la conversation");
        User recipient = req.senderId().equals(aId) ? conv.getUserB() : conv.getUserA();
        return messageService.send(conv, sender, req.content(), recipient);
    }

    @PatchMapping("/{id}")
    public Message edit(@PathVariable Long id, @RequestBody @Valid Dtos.EditMessageRequest req) {
        Message m = require(id);
        Conversation conv = m.getConversation();
        User recipient = m.getSender().getId().equals(conv.getUserA().getId()) ? conv.getUserB() : conv.getUserA();
        return messageService.edit(m, req.content(), recipient);
    }

    @DeleteMapping("/{id}")
    public Message delete(@PathVariable Long id) {
        Message m = require(id);
        Conversation conv = m.getConversation();
        User recipient = m.getSender().getId().equals(conv.getUserA().getId()) ? conv.getUserB() : conv.getUserA();
        return messageService.softDelete(m, recipient);
    }

    @GetMapping("/conversation/{conversationId}")
    public List<Message> list(@PathVariable Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));
        return messageService.list(conv);
    }

    private Message require(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable"));
    }
}
