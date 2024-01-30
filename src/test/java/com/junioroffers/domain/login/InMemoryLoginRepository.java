package com.junioroffers.domain.login;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLoginRepository implements LoginRepository {

    Map<String, User> inMemoryUserDatabase = new ConcurrentHashMap<>();

    @Override
    public User save(User userToSave) {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(String.valueOf(id))
                .username(userToSave.username())
                .password(userToSave.password())
                .build();
        inMemoryUserDatabase.put(userToSave.username(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(inMemoryUserDatabase.get(username));
    }
}
