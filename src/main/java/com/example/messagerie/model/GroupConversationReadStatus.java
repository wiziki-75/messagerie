package com.example.messagerie.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_conversation_read_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_conversation_id"}))
public class GroupConversationReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private GroupConversation groupConversation;

    @Column(nullable = false)
    private LocalDateTime lastReadAt = LocalDateTime.now();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public GroupConversation getGroupConversation() { return groupConversation; }
    public void setGroupConversation(GroupConversation groupConversation) { this.groupConversation = groupConversation; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}
