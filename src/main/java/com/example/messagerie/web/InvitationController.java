package com.example.messagerie.web;

import com.example.messagerie.model.Friendship;
import com.example.messagerie.model.InvitationCode;
import com.example.messagerie.model.User;
import com.example.messagerie.service.InvitationService;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    public InvitationController(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }

    @PostMapping("/generate")
    public Dtos.CodeResponse generate(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.requireByUsername(userDetails.getUsername());
        InvitationCode inv = invitationService.generate(user);
        return new Dtos.CodeResponse(inv.getCode());
    }

    @PostMapping("/use")
    public Friendship use(@RequestBody Dtos.UseCodeRequest req,
                          @AuthenticationPrincipal UserDetails userDetails) {
        User requester = userService.requireByUsername(userDetails.getUsername());
        return invitationService.use(req.code(), requester);
    }
}
