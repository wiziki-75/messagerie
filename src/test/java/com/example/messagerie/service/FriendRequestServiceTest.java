package com.example.messagerie.service;

import com.example.messagerie.model.*;
import com.example.messagerie.repository.FriendshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendRequestServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FriendRequestService service;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(1L);
        alice.setName("Alice");

        bob = new User();
        bob.setId(2L);
        bob.setName("Bob");
    }

    @Test
    void send_rejectsSelfRequest() {
        assertThatThrownBy(() -> service.send(alice, alice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible");
        verifyNoInteractions(notificationService);
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void send_rejectsWhenExistingInEitherDirection() {
        when(friendshipRepository.findByFromUserAndToUser(alice, bob)).thenReturn(Optional.of(new Friendship()));
        when(friendshipRepository.findByFromUserAndToUser(bob, alice)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.send(alice, bob))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existe déjà");

        // Ou bien si inversé
        reset(friendshipRepository);
        when(friendshipRepository.findByFromUserAndToUser(alice, bob)).thenReturn(Optional.empty());
        when(friendshipRepository.findByFromUserAndToUser(bob, alice)).thenReturn(Optional.of(new Friendship()));

        assertThatThrownBy(() -> service.send(alice, bob))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(notificationService);
    }

    @Test
    void send_createsPending_andNotifiesRecipient() {
        when(friendshipRepository.findByFromUserAndToUser(alice, bob)).thenReturn(Optional.empty());
        when(friendshipRepository.findByFromUserAndToUser(bob, alice)).thenReturn(Optional.empty());

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        Friendship saved = service.send(alice, bob);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getFromUser()).isEqualTo(alice);
        assertThat(saved.getToUser()).isEqualTo(bob);
        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.PENDING);

        verify(notificationService).notify(bob, NotificationType.FRIEND_REQUEST_RECEIVED, 10L, "Demande d'ami de Alice");
    }

    @Test
    void accept_setsAccepted_andNotifiesSender() {
        Friendship req = new Friendship();
        req.setId(5L);
        req.setFromUser(alice);
        req.setToUser(bob);
        req.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship saved = service.accept(req);

        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        assertThat(saved.getUpdatedAt()).isNotNull();
        verify(notificationService).notify(alice, NotificationType.FRIEND_REQUEST_ACCEPTED, 5L, "Bob a accepté votre demande");
        verify(friendshipRepository).save(req);
    }

    @Test
    void decline_setsDeclined_andNotifiesSender() {
        Friendship req = new Friendship();
        req.setId(6L);
        req.setFromUser(alice);
        req.setToUser(bob);
        req.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship saved = service.decline(req);

        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.DECLINED);
        assertThat(saved.getUpdatedAt()).isNotNull();
        verify(notificationService).notify(alice, NotificationType.FRIEND_REQUEST_DECLINED, 6L, "Bob a décliné votre demande");
        verify(friendshipRepository).save(req);
    }

    @Test
    void cancel_setsCanceled_withoutNotification() {
        Friendship req = new Friendship();
        req.setId(7L);
        req.setFromUser(alice);
        req.setToUser(bob);
        req.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship saved = service.cancel(req);
        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.CANCELED);
        assertThat(saved.getUpdatedAt()).isNotNull();
        verify(friendshipRepository).save(req);
        verifyNoInteractions(notificationService);
    }

    @Test
    void list_methods_delegateToRepository() {
        when(friendshipRepository.findByToUserAndStatus(bob, FriendshipStatus.PENDING)).thenReturn(List.of(new Friendship()));
        when(friendshipRepository.findByFromUserAndStatus(alice, FriendshipStatus.PENDING)).thenReturn(List.of(new Friendship()));
        when(friendshipRepository.findAcceptedFriendships(alice)).thenReturn(List.of(new Friendship()));

        assertThat(service.listReceived(bob)).hasSize(1);
        assertThat(service.listSent(alice)).hasSize(1);
        assertThat(service.listFriends(alice)).hasSize(1);

        verify(friendshipRepository).findByToUserAndStatus(bob, FriendshipStatus.PENDING);
        verify(friendshipRepository).findByFromUserAndStatus(alice, FriendshipStatus.PENDING);
        verify(friendshipRepository).findAcceptedFriendships(alice);
    }
}
