package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;

@Slf4j
public class UserValidator {
    public static void validateUser(User user, HashMap<Integer, User> users, RequestMethod method) {
        if (!users.containsKey(user.getId()) && method.equals(RequestMethod.PUT)) {
            log.error("ValidationException: {}", "User with id: " + user.getId() + " does not exist");
            throw new ValidationException("User with id: " + user.getId() + " does not exist");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("ValidationException: {}", "User email shouldn't not be blank");
            throw new ValidationException("User email shouldn't not be blank");
        }
        if (!user.getEmail().contains("@")) {
            log.error("ValidationException: {}", "User email should contain '@' symbol");
            throw new ValidationException("User email should contain '@' symbol");
        }
        if (user.getEmail() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("ValidationException: {}", "User login shouldn't be blank or contain spaces");
            throw new ValidationException("User login shouldn't be blank or contain spaces");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("ValidationException: {}", "User birthday shouldn't be after current date");
            throw new ValidationException("User birthday shouldn't be after current date");
        }
    }
}
