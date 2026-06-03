package com.example.messagerie.web;

import com.example.messagerie.model.Friendship;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.FriendshipRepository;
import com.example.messagerie.service.FriendRequestService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
public class FriendRequestController {
    private final FriendRequestService friendRequestService;
    private final UserService userService;
    private final FriendshipRepository friendshipRepository;

    public FriendRequestController(FriendRequestService friendRequestService,
                                   UserService userService,
                                   FriendshipRepository friendshipRepository) {
        this.friendRequestService = friendRequestService;
        this.userService = userService;
        this.friendshipRepository = friendshipRepository;
    }

    @PostMapping("/send")
    public Friendship send(@RequestBody Dtos.SendFriendRequest req) {
        User from = userService.require(req.fromUserId());
        User to = userService.require(req.toUserId());
        return friendRequestService.send(from, to);
    }

    @PatchMapping("/{id}/accept")
    public Friendship accept(@PathVariable Long id) {
        Friendship f = require(id);
        return friendRequestService.accept(f);
    }

    @PatchMapping("/{id}/decline")
    public Friendship decline(@PathVariable Long id) {
        Friendship f = require(id);
        return friendRequestService.decline(f);
    }

    @PatchMapping("/{id}/cancel")
    public Friendship cancelPatch(@PathVariable Long id) {
        Friendship f = require(id);
        return friendRequestService.cancel(f);
    }

    @DeleteMapping("/{id}")
    public Friendship cancel(@PathVariable Long id) {
        Friendship f = require(id);
        return friendRequestService.cancel(f);
    }

    @GetMapping("/received/{userId}")
    public List<Friendship> listReceived(@PathVariable Long userId) {
        User to = userService.require(userId);
        return friendRequestService.listReceived(to);
    }

    @GetMapping("/sent/{userId}")
    public List<Friendship> listSent(@PathVariable Long userId) {
        User from = userService.require(userId);
        return friendRequestService.listSent(from);
    }

    @GetMapping("/friends/{userId}")
    public List<Friendship> listFriends(@PathVariable Long userId) {
        User user = userService.require(userId);
        return friendRequestService.listFriends(user);
    }

    private Friendship require(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
    }
}
