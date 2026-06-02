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

    public FriendRequestController(FriendRequestService friendRequestService, UserService userService, FriendshipRepository friendshipRepository) {
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

    @PostMapping("/accept")
    public Friendship accept(@RequestBody Dtos.UpdateFriendRequest req) {
        Friendship f = friendshipRepository.findById(req.requestId()).orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        return friendRequestService.accept(f);
    }

    @PostMapping("/decline")
    public Friendship decline(@RequestBody Dtos.UpdateFriendRequest req) {
        Friendship f = friendshipRepository.findById(req.requestId()).orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        return friendRequestService.decline(f);
    }

    @PostMapping("/cancel")
    public Friendship cancel(@RequestBody Dtos.UpdateFriendRequest req) {
        Friendship f = friendshipRepository.findById(req.requestId()).orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        return friendRequestService.cancel(f);
    }

    @GetMapping("/received/{userId}")
    public List<Friendship> listReceived(@PathVariable Long userId) {
        User to = userService.require(userId);
        return friendRequestService.listReceived(to);
    }
}
