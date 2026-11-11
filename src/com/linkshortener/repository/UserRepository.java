
package com.linkshortener.repository;

import com.linkshortener.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();
}
