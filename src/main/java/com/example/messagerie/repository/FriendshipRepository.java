package com.example.messagerie.repository;

import com.example.messagerie.model.Friendship;
import com.example.messagerie.model.FriendshipStatus;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByFromUserAndToUser(User from, User to);
    List<Friendship> findByToUserAndStatus(User toUser, FriendshipStatus status);
    List<Friendship> findByFromUserOrToUserAndStatus(User from, User to, FriendshipStatus status);
}
