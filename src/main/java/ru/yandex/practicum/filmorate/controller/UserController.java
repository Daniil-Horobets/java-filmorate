package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter = 0;
    // Получение списка всех пользователей.
    @GetMapping
    public List<User> findAll() {
        log.info("Request endpoint: 'GET /users'");
        return new ArrayList<>(users.values());
    }

    // Создание пользователя
    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Request endpoint: 'POST /users'");
        UserValidator.validateUser(user, users, RequestMethod.POST);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(++idCounter);
        users.put(idCounter, user);
        return user;
    }

    // Обновление пользователя
    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Request endpoint: 'PUT /users'");
        UserValidator.validateUser(user, users, RequestMethod.PUT);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }
}
