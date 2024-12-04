package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        log.info("Пользователь из тела POST запроса - {}", user);
        User createdUser = userService.create(user);
        log.info("Пользователь пред ответом - {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Пользователь из тела PUT запроса - {}", user);
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(
            @PathVariable("id") long userId,
            @PathVariable("friendId") long friendId
    ) {
        log.info("Id пользователей из параметров запроса для добавления в друзья: userId={} friendId={}",
                userId, friendId);
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(
            @PathVariable("id") long userId,
            @PathVariable("friendId") long friendId
    ) {
        return userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable("id") long userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(
            @PathVariable("id") long userId,
            @PathVariable("otherId") long otherId
    ) {
        return userService.getMutualFriends(userId, otherId);
    }


}
