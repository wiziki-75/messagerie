package com.example.messagerie.service;

import com.example.messagerie.model.Conversation;
import com.example.messagerie.model.ConversationReadStatus;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.ConversationReadStatusRepository;
import com.example.messagerie.repository.ConversationRepository;
import com.example.messagerie.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationReadStatusRepository readStatusRepository;
    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ConversationService service;

    private User user1;
    private User user2;

    @Captor
    private ArgumentCaptor<ConversationReadStatus> statusCaptor;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("U1");
        user1.setUsername("u1");
        user1.setPasswordHash("x");

        user2 = new User();
        user2.setId(2L);
        user2.setName("U2");
        user2.setUsername("u2");
        user2.setPasswordHash("y");
    }

    @Test
    void getOrCreate_returnsExistingRegardlessOfOrder() {
        Conversation existing = new Conversation();
        existing.setId(10L);
        existing.setUserA(user1);
        existing.setUserB(user2);

        when(conversationRepository.findByUserAAndUserB(user1, user2))
                .thenReturn(Optional.of(existing));

        Conversation c1 = service.getOrCreate(user1, user2);
        Conversation c2 = service.getOrCreate(user2, user1); // inversé

        assertThat(c1).isSameAs(existing);
        assertThat(c2).isSameAs(existing);
        verify(conversationRepository, times(2)).findByUserAAndUserB(user1, user2);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void getOrCreate_createsWhenAbsent_andOrdersById() {
        when(conversationRepository.findByUserAAndUserB(user1, user2))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(inv -> {
                    Conversation c = inv.getArgument(0);
                    c.setId(99L);
                    return c;
                });

        Conversation created = service.getOrCreate(user2, user1); // ordre inversé en entrée

        assertThat(created.getId()).isEqualTo(99L);
        assertThat(created.getUserA()).isEqualTo(user1); // plus petit id en premier
        assertThat(created.getUserB()).isEqualTo(user2);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void require_returnsConversationWhenFound() {
        Conversation conv = new Conversation();
        conv.setId(7L);
        when(conversationRepository.findById(7L)).thenReturn(Optional.of(conv));

        Conversation got = service.require(7L);
        assertThat(got).isSameAs(conv);
    }

    @Test
    void require_throwsWhenNotFound() {
        when(conversationRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.require(123L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("123");
    }

    @Test
    void listFor_delegatesToRepository() {
        Conversation c = new Conversation();
        when(conversationRepository.findByUserAOrUserB(user1, user1))
                .thenReturn(List.of(c));

        List<Conversation> list = service.listFor(user1);
        assertThat(list).containsExactly(c);
        verify(conversationRepository).findByUserAOrUserB(user1, user1);
    }

    @Test
    void markRead_createsStatusWhenAbsent() {
        Conversation conv = new Conversation();
        conv.setId(5L);
        when(readStatusRepository.findByUserAndConversation(user1, conv))
                .thenReturn(Optional.empty());

        service.markRead(conv, user1);

        verify(readStatusRepository).save(statusCaptor.capture());
        ConversationReadStatus saved = statusCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user1);
        assertThat(saved.getConversation()).isEqualTo(conv);
        assertThat(saved.getLastReadAt()).isNotNull();
        assertThat(saved.getLastReadAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void markRead_updatesExistingStatusTimestamp() {
        Conversation conv = new Conversation();
        conv.setId(6L);
        ConversationReadStatus existing = new ConversationReadStatus();
        existing.setUser(user1);
        existing.setConversation(conv);
        LocalDateTime old = LocalDateTime.now().minusDays(1);
        existing.setLastReadAt(old);

        when(readStatusRepository.findByUserAndConversation(user1, conv))
                .thenReturn(Optional.of(existing));

        service.markRead(conv, user1);

        verify(readStatusRepository).save(statusCaptor.capture());
        ConversationReadStatus saved = statusCaptor.getValue();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getLastReadAt()).isAfter(old);
    }

    @Test
    void unreadCount_usesLastReadFromStatus() {
        Conversation conv = new Conversation();
        conv.setId(8L);
        LocalDateTime since = LocalDateTime.now().minusHours(2);
        ConversationReadStatus s = new ConversationReadStatus();
        s.setUser(user1);
        s.setConversation(conv);
        s.setLastReadAt(since);

        when(readStatusRepository.findByUserAndConversation(user1, conv))
                .thenReturn(Optional.of(s));
        when(messageRepository.countByConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(conv, user1, since))
                .thenReturn(3);

        int count = service.unreadCount(conv, user1);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void unreadCount_usesEpochWhenNoStatus() {
        Conversation conv = new Conversation();
        conv.setId(9L);

        when(readStatusRepository.findByUserAndConversation(user1, conv))
                .thenReturn(Optional.empty());

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(messageRepository.countByConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(eq(conv), eq(user1), any(LocalDateTime.class)))
                .thenReturn(0);

        service.unreadCount(conv, user1);

        verify(messageRepository).countByConversationAndSenderNotAndCreatedAtAfterAndDeletedFalse(eq(conv), eq(user1), sinceCaptor.capture());
        LocalDateTime used = sinceCaptor.getValue();
        assertThat(used).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0));
    }
}
