package com.example.messagerie.web;

import com.example.messagerie.model.User;
import com.example.messagerie.service.UserService;
import com.example.messagerie.web.dto.Dtos;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Dtos.IdResponse> create(@RequestBody @Validated Dtos.CreateUserRequest req) {
        User u = userService.create(req.name());
        return ResponseEntity.created(URI.create("/api/users/" + u.getId())).body(new Dtos.IdResponse(u.getId()));
    }

    @GetMapping
    public List<User> list() { return userService.list(); }
}
