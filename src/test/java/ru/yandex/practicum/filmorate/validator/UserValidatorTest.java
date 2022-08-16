package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("email@mail.com");
        user.setName("Name");
        user.setBirthday(LocalDate.now());
        user.setLogin("Login");
    }

    @Test
    void userWithBlankEmailValidation() {
        user.setEmail("");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user));
    }

    @Test
    void userWithEmailWithoutAtSymbolValidation() {
        user.setEmail("email.com");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user));
    }

    @Test
    void userWithBlankOrContainingSpacesLoginValidation() {
        user.setLogin("");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user));

        user.setLogin("Log in");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user));
    }

    @Test
    void userWithBirthdayAfterNowValidation() {
        user.setBirthday(LocalDate.MAX);

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user));
    }

    @Test
    void createEmptyUser() {
        User user1 = new User();

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user1));
    }
}