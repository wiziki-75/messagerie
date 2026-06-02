package com.example.messagerie.service;

import com.example.messagerie.model.*;
import com.example.messagerie.repository.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendRequestService {
    private final FriendshipRepository friendshipRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;

    public FriendRequestService(FriendshipRepository friendshipRepository,
                                ConversationService conversationService,
                                NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.conversationService = conversationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Friendship send(User from, User to) {
        if (from.getId().equals(to.getId())) throw new IllegalArgumentException("Impossible de s'envoyer une demande à soi-même");
        friendshipRepository.findByFromUserAndToUser(from, to).ifPresent(f -> {
            throw new IllegalArgumentException("Demande déjà existante");
        });
        Friendship f = new Friendship();
        f.setFromUser(from);
        f.setToUser(to);
        f.setStatus(FriendshipStatus.PENDING);
        Friendship saved = friendshipRepository.save(f);
        notificationService.notify(to, NotificationType.FRIEND_REQUEST_RECEIVED, saved.getId(), "Demande d'ami de " + from.getName());
        return saved;
    }

    @Transactional
    public Friendship accept(Friendship req) {
        req.setStatus(FriendshipStatus.ACCEPTED);
        req.setUpdatedAt(LocalDateTime.now());
        Friendship saved = friendshipRepository.save(req);
        // create conversation when accepted
        conversationService.getOrCreate(req.getFromUser(), req.getToUser());
        notificationService.notify(req.getFromUser(), NotificationType.FRIEND_REQUEST_ACCEPTED, saved.getId(), req.getToUser().getName() + " a accepté votre demande");
        return saved;
    }

    @Transactional
    public Friendship decline(Friendship req) {
        req.setStatus(FriendshipStatus.DECLINED);
        req.setUpdatedAt(LocalDateTime.now());
        Friendship saved = friendshipRepository.save(req);
        notificationService.notify(req.getFromUser(), NotificationType.FRIEND_REQUEST_DECLINED, saved.getId(), req.getToUser().getName() + " a décliné votre demande");
        return saved;
    }

    @Transactional
    public Friendship cancel(Friendship req) {
        req.setStatus(FriendshipStatus.CANCELED);
        req.setUpdatedAt(LocalDateTime.now());
        return friendshipRepository.save(req);
    }

    @Transactional(readOnly = true)
    public List<Friendship> listReceived(User toUser) {
        return friendshipRepository.findByToUserAndStatus(toUser, FriendshipStatus.PENDING);
    }
}
