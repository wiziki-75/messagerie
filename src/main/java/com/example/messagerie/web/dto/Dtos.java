package com.example.messagerie.web.dto;

import com.example.messagerie.model.User;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public class Dtos {
    public record CreateUserRequest(String name, String username, String password) {}
    public record IdResponse(Long id) {}

    public record SendFriendRequest(Long fromUserId, Long toUserId) {}

    public record GetOrCreateConversationRequest(Long userAId, Long userBId) {}

    public record SendMessageRequest(Long conversationId, Long senderId, @NotBlank String content) {}
    public record EditMessageRequest(@NotBlank String content) {}

    public record UploadFileResponse(Long id, String originalName, long size) {}

    public record CodeResponse(String code) {}
    public record UseCodeRequest(String code) {}

    public record ErrorResponse(String error, String message) {}

    public record ConversationWithUnread(Long id, User userA, User userB, LocalDateTime createdAt, int unreadCount) {}

    public record GroupConversationDto(Long id, String name, User creator, List<User> members, LocalDateTime createdAt, int unreadCount) {}

    public record CreateGroupConversationRequest(Long creatorId, @NotBlank String name, List<Long> memberIds) {}
    public record SendGroupMessageRequest(Long senderId, @NotBlank String content) {}
}
