package com.example.messagerie.web.dto;

import jakarta.validation.constraints.NotBlank;

public class Dtos {
    public record CreateUserRequest(String name, String username, String password) {}
    public record IdResponse(Long id) {}

    public record SendFriendRequest(Long fromUserId, Long toUserId) {}

    public record GetOrCreateConversationRequest(Long userAId, Long userBId) {}

    public record SendMessageRequest(Long conversationId, Long senderId, @NotBlank String content) {}
    public record EditMessageRequest(@NotBlank String content) {}

    public record UploadFileResponse(Long id, String originalName, long size) {}

    public record ErrorResponse(String error, String message) {}
}
