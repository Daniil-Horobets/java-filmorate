package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {
    public static void validateUser(User user) {
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
