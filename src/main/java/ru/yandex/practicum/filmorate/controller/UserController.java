package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
        validateUser(user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        idCounter++;
        user.setId(idCounter);
        users.put(idCounter, user);
        return user;
    }

    // Обновление пользователя
    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Request endpoint: 'PUT /users'");
        validateUser(user);
        if (!users.containsKey(user.getId())) {
            String exceptionMessage = "User with id: " + user.getId() + "does not exist";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    protected void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            String exceptionMessage = "User email shouldn't not be blank";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (!user.getEmail().contains("@")) {
            String exceptionMessage = "User email should contain '@' symbol";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (user.getEmail() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            String exceptionMessage = "User login shouldn't be blank or contain spaces";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            String exceptionMessage = "User birthday shouldn't be after current date";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
    }
}
