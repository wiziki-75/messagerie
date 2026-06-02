package com.example.messagerie.web.dto;

public class Dtos {
    public record CreateUserRequest(String name, String username, String password) {}
    public record IdResponse(Long id) {}

    public record SendFriendRequest(Long fromUserId, Long toUserId) {}
    public record UpdateFriendRequest(Long requestId) {}

    public record GetOrCreateConversationRequest(Long userAId, Long userBId) {}

    public record SendMessageRequest(Long conversationId, Long senderId, String content) {}
    public record EditMessageRequest(Long messageId, String content) {}
    public record DeleteMessageRequest(Long messageId) {}

    public record UploadFileResponse(Long id, String originalName, long size) {}

    public record DeleteFileRequest(Long fileId) {}

    public record ErrorResponse(String error, String message) {}
}
