package com.example.messagerie.config;

import com.example.messagerie.model.*;
import com.example.messagerie.repository.*;
import com.example.messagerie.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seed(
            UserService userService,
            FriendRequestService friendRequestService,
            ConversationService conversationService,
            MessageService messageService
    ) {
        return args -> {
            // ── Utilisateurs ──────────────────────────────────────────────────
            User alice   = userService.create("Alice",   "alice",   "password123");
            User bob     = userService.create("Bob",     "bob",     "password123");
            User charlie = userService.create("Charlie", "charlie", "password123");
            User diana   = userService.create("Diana",   "diana",   "password123");
            User eve     = userService.create("Eve",     "eve",     "password123");

            log.info("✅ Utilisateurs créés : Alice({}), Bob({}), Charlie({}), Diana({}), Eve({})",
                    alice.getId(), bob.getId(), charlie.getId(), diana.getId(), eve.getId());

            // ── Alice ↔ Bob : amis + conversation + messages ──────────────────
            Friendship aliceBob = friendRequestService.send(alice, bob);
            friendRequestService.accept(aliceBob);
            Conversation convAliceBob = conversationService.getOrCreate(alice, bob);

            messageService.send(convAliceBob, alice, "Salut Bob, ça va ?",            bob);
            messageService.send(convAliceBob, bob,   "Super et toi ?",                alice);
            messageService.send(convAliceBob, alice, "Nickel ! On se voit ce soir ?", bob);
            messageService.send(convAliceBob, bob,   "Avec plaisir 🎉",              alice);

            log.info("✅ Alice ↔ Bob : amis + 4 messages");

            // ── Alice ↔ Charlie : amis + conversation + messages ─────────────
            Friendship aliceCharlie = friendRequestService.send(alice, charlie);
            friendRequestService.accept(aliceCharlie);
            Conversation convAliceCharlie = conversationService.getOrCreate(alice, charlie);

            messageService.send(convAliceCharlie, charlie, "Hey Alice !",      alice);
            messageService.send(convAliceCharlie, alice,   "Coucou Charlie !", charlie);

            log.info("✅ Alice ↔ Charlie : amis + 2 messages");

            // ── Bob ↔ Charlie : amis + conversation ──────────────────────────
            Friendship bobCharlie = friendRequestService.send(bob, charlie);
            friendRequestService.accept(bobCharlie);
            Conversation convBobCharlie = conversationService.getOrCreate(bob, charlie);
            messageService.send(convBobCharlie, bob, "Yo Charlie !", charlie);

            log.info("✅ Bob ↔ Charlie : amis + 1 message");

            // ── Demandes en attente ───────────────────────────────────────────
            friendRequestService.send(diana,   alice);
            friendRequestService.send(eve,     bob);
            friendRequestService.send(charlie, diana);

            log.info("✅ Demandes en attente : Diana→Alice, Eve→Bob, Charlie→Diana");
            log.info("🚀 Données de test prêtes — ouvrez http://localhost:8080");
        };
    }
}
