package com.example.messagerie.service;

import com.example.messagerie.model.*;
import com.example.messagerie.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupConversationService {
    private final GroupConversationRepository groupRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupConversationReadStatusRepository readStatusRepository;
    private final NotificationService notificationService;

    public GroupConversationService(GroupConversationRepository groupRepository,
                                    GroupMessageRepository groupMessageRepository,
                                    GroupConversationReadStatusRepository readStatusRepository,
                                    NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public GroupConversation create(User creator, String name, List<User> members) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide");
        if (members.size() < 1)
            throw new IllegalArgumentException("Un groupe doit avoir au moins un autre membre");

        GroupConversation group = new GroupConversation();
        group.setName(name.trim());
        group.setCreator(creator);
        group.getMembers().add(creator);
        for (User m : members) {
            if (!group.getMembers().contains(m)) group.getMembers().add(m);
        }
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public GroupConversation require(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<GroupConversation> listFor(User user) {
        return groupRepository.findByMember(user);
    }

    @Transactional(readOnly = true)
    public List<GroupMessage> listMessages(GroupConversation group) {
        return groupMessageRepository.findByGroupConversationOrderByCreatedAtAsc(group);
    }

    @Transactional
    public GroupMessage sendMessage(GroupConversation group, User sender, String content) {
        if (!group.getMembers().contains(sender))
            throw new IllegalArgumentException("Vous n'êtes pas membre de ce groupe");

        GroupMessage msg = new GroupMessage();
        msg.setGroupConversation(group);
        msg.setSender(sender);
        msg.setContent(content);
        GroupMessage saved = groupMessageRepository.save(msg);

        String payload = sender.getName() + " : " + (content.length() > 60 ? content.substring(0, 60) + "…" : content);
        group.getMembers().stream()
                .filter(m -> !m.getId().equals(sender.getId()))
                .forEach(m -> notificationService.notify(m, NotificationType.GROUP_MESSAGE_RECEIVED, saved.getId(), "[" + group.getName() + "] " + payload));

        return saved;
    }

    @Transactional
    public GroupMessage deleteMessage(GroupMessage msg, User requester) {
        if (!msg.getSender().getId().equals(requester.getId()))
            throw new IllegalArgumentException("Vous ne pouvez supprimer que vos propres messages");
        msg.setDeleted(true);
        return groupMessageRepository.save(msg);
    }

    @Transactional(readOnly = true)
    public GroupMessage requireMessage(Long id) {
        return groupMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable : " + id));
    }

    @Transactional
    public void markRead(GroupConversation group, User user) {
        GroupConversationReadStatus status = readStatusRepository
                .findByUserAndGroupConversation(user, group)
                .orElse(new GroupConversationReadStatus());
        status.setUser(user);
        status.setGroupConversation(group);
        status.setLastReadAt(LocalDateTime.now());
        readStatusRepository.save(status);
    }

    @Transactional(readOnly = true)
    public int unreadCount(GroupConversation group, User currentUser) {
        LocalDateTime since = readStatusRepository
                .findByUserAndGroupConversation(currentUser, group)
                .map(GroupConversationReadStatus::getLastReadAt)
                .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));
        return groupMessageRepository.countByGroupConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(group, currentUser, since);
    }
}
