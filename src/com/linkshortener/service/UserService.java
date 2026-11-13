package com.linkshortener.service;

import com.linkshortener.model.User;
import com.linkshortener.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "UserRepository не может быть null."
        );
    }

    public User createUser() {
        User user = new User();

        userRepository.save(user);

        return user;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
