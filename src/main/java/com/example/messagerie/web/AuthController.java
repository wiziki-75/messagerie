package com.example.messagerie.web;

import com.example.messagerie.model.User;
import com.example.messagerie.repository.UserRepository;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Dtos.IdResponse> register(@RequestBody Dtos.CreateUserRequest req) {
        User u = userService.create(req.name(), req.username(), req.password());
        return ResponseEntity.created(URI.create("/api/users/" + u.getId())).body(new Dtos.IdResponse(u.getId()));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        String username = authentication.getName();
        User u = userRepository.findByUsername(username).orElse(null);
        return Map.of(
                "username", username,
                "userId", u == null ? null : u.getId(),
                "name", u == null ? null : u.getName()
        );
    }
}
