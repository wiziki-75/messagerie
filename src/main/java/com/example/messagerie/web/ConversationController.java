package com.example.messagerie.web;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.User;
import com.example.messagerie.service.ConversationService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final UserService userService;

    public ConversationController(ConversationService conversationService, UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    public List<Dtos.ConversationWithUnread> listFor(@PathVariable Long userId) {
        User u = userService.require(userId);
        return conversationService.listFor(u).stream()
                .map(c -> new Dtos.ConversationWithUnread(
                        c.getId(), c.getUserA(), c.getUserB(), c.getCreatedAt(),
                        conversationService.unreadCount(c, u)))
                .toList();
    }

    @PostMapping("/get-or-create")
    public Conversation getOrCreate(@RequestBody Dtos.GetOrCreateConversationRequest req) {
        User a = userService.require(req.userAId());
        User b = userService.require(req.userBId());
        return conversationService.getOrCreate(a, b);
    }

    @PostMapping("/{id}/mark-read")
    public void markRead(@PathVariable Long id, Authentication auth) {
        Conversation conv = conversationService.require(id);
        User user = userService.requireByUsername(auth.getName());
        conversationService.markRead(conv, user);
    }
}
