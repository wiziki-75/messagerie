package com.example.messagerie.repository;

import com.example.messagerie.model.Friendship;
import com.example.messagerie.model.FriendshipStatus;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByFromUserAndToUser(User from, User to);
    List<Friendship> findByToUserAndStatus(User toUser, FriendshipStatus status);
    List<Friendship> findByFromUserAndStatus(User fromUser, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.fromUser = :user OR f.toUser = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);
}
