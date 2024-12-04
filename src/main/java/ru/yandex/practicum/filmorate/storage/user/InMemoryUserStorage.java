package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.HaveLongIdInSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage, HaveLongIdInSet {
    private static final Map<Long, User> users = new HashMap<>();


    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User add(User user) {
        user.setId(getNextId(users.keySet()));
        log.info("Пользователь перед сохранением при создании - {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean isContainsEmail(String email) {
        return users.values().stream()
                .map(User::getEmail)
                .anyMatch(email::equals);
    }

    @Override
    public User addFriend(long userId, long friendId) {
        get(friendId).getFriends().add(userId);
        get(userId).getFriends().add(friendId);
        log.info("Список друзей после добавления в друзья пользователя 1={}, пользователя 2={}",
                get(userId).getFriends(),
                get(friendId).getFriends()
        );
        return get(userId);
    }

    @Override
    public User deleteFriend(long userId, long friendId) {
        users.get(friendId).getFriends().remove(userId);
        users.get(userId).getFriends().remove(friendId);
        return users.get(userId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        return users.get(userId).getFriends().stream()
                .map(users::get)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<User> getMutualFriends(long userId, long otherId) {
        return users.get(userId).getFriends().stream()
                .filter(id -> users.get(otherId).getFriends().contains(id))
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public User get(long id) {
        return users.get(id);
    }

    @Override
    public boolean isContains(long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean delete(long id) {
        return false;
    }
}
