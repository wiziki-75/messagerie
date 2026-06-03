package com.example.messagerie.web;

import com.example.messagerie.model.GroupConversation;
import com.example.messagerie.model.GroupMessage;
import com.example.messagerie.model.User;
import com.example.messagerie.service.GroupConversationService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-conversations")
public class GroupConversationController {
    private final GroupConversationService groupService;
    private final UserService userService;

    public GroupConversationController(GroupConversationService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @PostMapping
    public Dtos.GroupConversationDto create(@RequestBody Dtos.CreateGroupConversationRequest req) {
        User creator = userService.require(req.creatorId());
        List<User> members = req.memberIds().stream().map(userService::require).toList();
        GroupConversation group = groupService.create(creator, req.name(), members);
        return toDto(group, creator);
    }

    @GetMapping("/user/{userId}")
    public List<Dtos.GroupConversationDto> listFor(@PathVariable Long userId) {
        User u = userService.require(userId);
        return groupService.listFor(u).stream()
                .map(g -> toDto(g, u))
                .toList();
    }

    @GetMapping("/{id}/messages")
    public List<GroupMessage> getMessages(@PathVariable Long id) {
        GroupConversation group = groupService.require(id);
        return groupService.listMessages(group);
    }

    @PostMapping("/{id}/messages")
    public GroupMessage sendMessage(@PathVariable Long id,
                                    @RequestBody Dtos.SendGroupMessageRequest req,
                                    Authentication auth) {
        GroupConversation group = groupService.require(id);
        User sender = userService.require(req.senderId());
        return groupService.sendMessage(group, sender, req.content());
    }

    @DeleteMapping("/{groupId}/messages/{messageId}")
    public GroupMessage deleteMessage(@PathVariable Long groupId,
                                      @PathVariable Long messageId,
                                      Authentication auth) {
        groupService.require(groupId);
        GroupMessage msg = groupService.requireMessage(messageId);
        User requester = userService.requireByUsername(auth.getName());
        return groupService.deleteMessage(msg, requester);
    }

    @PostMapping("/{id}/mark-read")
    public void markRead(@PathVariable Long id, Authentication auth) {
        GroupConversation group = groupService.require(id);
        User user = userService.requireByUsername(auth.getName());
        groupService.markRead(group, user);
    }

    private Dtos.GroupConversationDto toDto(GroupConversation g, User currentUser) {
        return new Dtos.GroupConversationDto(
                g.getId(), g.getName(), g.getCreator(), g.getMembers(), g.getCreatedAt(),
                groupService.unreadCount(g, currentUser));
    }
}
