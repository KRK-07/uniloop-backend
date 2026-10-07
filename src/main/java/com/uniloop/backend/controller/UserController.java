package com.uniloop.backend.controller;

import com.uniloop.backend.entity.User;
import com.uniloop.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginUser) {

        User user = userRepository.findByEmail(loginUser.getEmail());

        if (user == null) {
            return "User not found";
        }

        if (!user.getPassword().equals(loginUser.getPassword())) {
            return "Invalid password";
        }

        return "Login successful";
    }
}