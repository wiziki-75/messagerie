package com.example.messagerie.service;

import com.example.messagerie.model.Friendship;
import com.example.messagerie.model.InvitationCode;
import com.example.messagerie.model.User;
import com.example.messagerie.repository.InvitationCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class InvitationService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvitationCodeRepository invitationCodeRepository;
    private final FriendRequestService friendRequestService;

    public InvitationService(InvitationCodeRepository invitationCodeRepository,
                             FriendRequestService friendRequestService) {
        this.invitationCodeRepository = invitationCodeRepository;
        this.friendRequestService = friendRequestService;
    }

    @Transactional
    public InvitationCode generate(User owner) {
        // Invalider tout code non utilisé existant
        List<InvitationCode> existing = invitationCodeRepository.findByOwnerAndUsed(owner, false);
        existing.forEach(c -> c.setUsed(true));
        invitationCodeRepository.saveAll(existing);

        InvitationCode inv = new InvitationCode();
        inv.setOwner(owner);
        inv.setCode(buildCode());
        return invitationCodeRepository.save(inv);
    }

    @Transactional
    public Friendship use(String rawCode, User requester) {
        String code = rawCode.trim().toUpperCase();
        InvitationCode inv = invitationCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Code invalide"));
        if (inv.isUsed())
            throw new IllegalArgumentException("Ce code a déjà été utilisé");
        if (inv.getOwner().getId().equals(requester.getId()))
            throw new IllegalArgumentException("Vous ne pouvez pas utiliser votre propre code");

        Friendship friendship = friendRequestService.send(requester, inv.getOwner());
        inv.setUsed(true);
        invitationCodeRepository.save(inv);
        return friendship;
    }

    private String buildCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) sb.append('-');
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
