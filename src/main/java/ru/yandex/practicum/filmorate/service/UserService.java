package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

@Slf4j
@Service
public class UserService {
    @Autowired
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User create(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.info("Не указан имейл - {}", user.getEmail());
            throw new ValidationException("Не указан имейл: email=" + user.getEmail());
        }

        checkContainsEmail(user.getEmail());
        emailValidate(user.getEmail());

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.info("Логин не может быть пустым - {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым: login=" + user.getLogin());
        }

        loginValidate(user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        birthdayValidate(user.getBirthday());
        user.setFriends(new HashSet<>());
        return userStorage.add(user);
    }

    public User update(User user) {

        if (user.getId() == null) {
            log.info("Не указан id пользователя");
            throw new ValidationException("Не указан id пользователя: id=" + user.getId());
        }

        if (!userStorage.isContains(user.getId())) {
            log.info("Пользователь с id - {}, не найден", user.getId());
            throw new NotFoundException("Пользователь с указанным id не найден: id=" + user.getId());
        }

        User oldUser = userStorage.get(user.getId());
        User updateUser = updateUser(user, oldUser);

        log.info("Пользователь перед сохранением при изменении - {}", updateUser);
        return userStorage.update(updateUser);
    }

    public User addFriend(long userId, long friendId) {
        checkContainsUsers(userId, friendId);
        return userStorage.addFriend(userId, friendId);
    }

    public User deleteFriend(long userId, long friendId) {
        checkContainsUsers(userId, friendId);
        return userStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        if (!userStorage.isContains(userId)) {
            throw new NotFoundException("Пользователя с таким id не существует: userId=" + userId);
        }
        return userStorage.getFriends(userId);
    }

    public Collection<User> getMutualFriends(long userId, long otherId) {
        checkContainsUsers(userId, otherId);
        return userStorage.getMutualFriends(userId, otherId);
    }

    private User updateUser(User user, User oldUser) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.setEmail(oldUser.getEmail());
        } else if (!user.getEmail().equals(oldUser.getEmail())) {
            emailValidate(user.getEmail());
            checkContainsEmail(user.getEmail());
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            user.setLogin(oldUser.getLogin());
        }

        if (user.getName() == null || user.getName().isBlank()) {
            if (oldUser.getLogin().equals(oldUser.getName())) {
                user.setName(user.getLogin());
            } else {
                user.setName(oldUser.getName());
            }
        }

        if (user.getBirthday() == null) {
            user.setBirthday(oldUser.getBirthday());
        } else {
            birthdayValidate(user.getBirthday());
        }
        user.setFriends(oldUser.getFriends());
        return user;
    }

    private void checkContainsUsers(long userId, long friendId) {
        if (!userStorage.isContains(userId)) {
            throw new NotFoundException("Пользователя с таким id не существует: userId=" + userId);
        }
        if (!userStorage.isContains(friendId)) {
            throw new NotFoundException("Пользователя с таким id не существует: userId=" + friendId);
        }
    }

    private void checkContainsEmail(String userEmail) {
        if (userStorage.isContainsEmail(userEmail)) {
            log.info("Пользователь с таким имейлом уже существует - {}", userEmail);
            throw new ValidationException("Пользователь с таким имейлом уже существует: userEmail=" + userEmail);
        }
    }

    private void emailValidate(String email) {
        if (!email.matches("\\w+([\\-._]?\\w+)*@\\w+([\\-.]\\w+)*\\.[A-Za-z]{2,4}")) {
            log.info("Указан некорректный имейл - {}", email);
            throw new ValidationException("Указан некорректный имейл: email=" + email);
        }
    }

    private void loginValidate(String login) {
        if (login.contains(" ")) {
            log.info("Логин не должен содержать пробелов, {}", login);
            throw new ValidationException("Логин не должен содержать пробелов: login=" + login);
        }
    }

    private void birthdayValidate(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            log.info("Дата рождения не может быть в будущем - {}", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем: birthday=" + birthday);
        }
    }
}
