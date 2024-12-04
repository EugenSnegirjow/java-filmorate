package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User add(User user);

    User get(long id);

    boolean isContains(long id);

    Collection<User> getAll();

    User update(User user);

    boolean delete(long id);

    boolean isContainsEmail(String email);

    User addFriend(long userId, long friendId);

    User deleteFriend(long userId, long friendId);

    Collection<User> getFriends(long userId);

    Collection<User> getMutualFriends(long userId, long friendId);
}
