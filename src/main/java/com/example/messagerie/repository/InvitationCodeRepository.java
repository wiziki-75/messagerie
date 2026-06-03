package com.example.messagerie.repository;

import com.example.messagerie.model.InvitationCode;
import com.example.messagerie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    Optional<InvitationCode> findByCode(String code);
    List<InvitationCode> findByOwnerAndUsed(User owner, boolean used);
}
