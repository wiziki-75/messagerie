package com.example.messagerie.web;

import com.example.messagerie.model.User;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Dtos.IdResponse> register(@RequestBody @Valid Dtos.CreateUserRequest req) {
        User u = userService.create(req.name(), req.username(), req.password());
        return ResponseEntity.created(URI.create("/api/users/" + u.getId())).body(new Dtos.IdResponse(u.getId()));
    }

    @GetMapping("/me")
    public User me(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.requireByUsername(userDetails.getUsername());
    }
}
