package com.example.messagerie.web;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.User;
import com.example.messagerie.service.ConversationService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
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
    public List<Conversation> listFor(@PathVariable Long userId) {
        User u = userService.require(userId);
        return conversationService.listFor(u);
    }

    @PostMapping("/get-or-create")
    public Conversation getOrCreate(@RequestBody Dtos.GetOrCreateConversationRequest req) {
        User a = userService.require(req.userAId());
        User b = userService.require(req.userBId());
        return conversationService.getOrCreate(a, b);
    }
}
