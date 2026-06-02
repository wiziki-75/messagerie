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
            MessageService messageService,
            UserRepository userRepository,
            FriendshipRepository friendshipRepository
    ) {
        return args -> {
            // ── Utilisateurs ──────────────────────────────────────────────────
            User alice   = userService.create("Alice");
            User bob     = userService.create("Bob");
            User charlie = userService.create("Charlie");
            User diana   = userService.create("Diana");
            User eve     = userService.create("Eve");

            log.info("✅ Utilisateurs créés : Alice({}), Bob({}), Charlie({}), Diana({}), Eve({})",
                    alice.getId(), bob.getId(), charlie.getId(), diana.getId(), eve.getId());

            // ── Alice ↔ Bob : amis + conversation + messages ──────────────────
            Friendship aliceBob = friendRequestService.send(alice, bob);
            friendRequestService.accept(aliceBob);
            Conversation convAliceBob = conversationService.getOrCreate(alice, bob);

            messageService.send(convAliceBob, alice, "Salut Bob, ça va ?", bob);
            messageService.send(convAliceBob, bob,   "Super et toi ?",     alice);
            messageService.send(convAliceBob, alice, "Nickel ! On se voit ce soir ?", bob);
            messageService.send(convAliceBob, bob,   "Avec plaisir 🎉",   alice);

            log.info("✅ Alice ↔ Bob : amis + 4 messages");

            // ── Alice ↔ Charlie : amis + conversation + messages ─────────────
            Friendship aliceCharlie = friendRequestService.send(alice, charlie);
            friendRequestService.accept(aliceCharlie);
            Conversation convAliceCharlie = conversationService.getOrCreate(alice, charlie);

            messageService.send(convAliceCharlie, charlie, "Hey Alice !",               alice);
            messageService.send(convAliceCharlie, alice,   "Coucou Charlie !",          charlie);

            log.info("✅ Alice ↔ Charlie : amis + 2 messages");

            // ── Bob ↔ Charlie : amis + conversation ──────────────────────────
            Friendship bobCharlie = friendRequestService.send(bob, charlie);
            friendRequestService.accept(bobCharlie);
            Conversation convBobCharlie = conversationService.getOrCreate(bob, charlie);
            messageService.send(convBobCharlie, bob, "Yo Charlie !", charlie);

            log.info("✅ Bob ↔ Charlie : amis + 1 message");

            // ── Diana → Alice : demande en attente ───────────────────────────
            friendRequestService.send(diana, alice);
            log.info("✅ Diana → Alice : demande en attente");

            // ── Eve → Bob : demande en attente ───────────────────────────────
            friendRequestService.send(eve, bob);
            log.info("✅ Eve → Bob : demande en attente");

            // ── Charlie → Diana : demande en attente ─────────────────────────
            friendRequestService.send(charlie, diana);
            log.info("✅ Charlie → Diana : demande en attente");

            log.info("🚀 Données de test prêtes — ouvrez http://localhost:8080");
        };
    }
}
