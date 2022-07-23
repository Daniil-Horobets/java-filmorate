package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private User user;

    @BeforeEach
    void init() {
        userController = new UserController();
        user = new User();
        user.setEmail("email@mail.com");
        user.setName("Name");
        user.setBirthday(LocalDate.now());
        user.setLogin("Login");
    }

    @Test
    void userWithBlankEmailValidation() {
        user.setEmail("");

        assertThrows(ValidationException.class, () -> userController.validateUser(user));
    }

    @Test
    void userWithEmailWithoutAtSymbolValidation() {
        user.setEmail("email.com");

        assertThrows(ValidationException.class, () -> userController.validateUser(user));
    }

    @Test
    void userWithBlankOrContainingSpacesLoginValidation() {
        user.setLogin("");

        assertThrows(ValidationException.class, () -> userController.validateUser(user));

        user.setLogin("Log in");

        assertThrows(ValidationException.class, () -> userController.validateUser(user));
    }

    @Test
    void userWithBirthdayAfterNowValidation() {
        user.setBirthday(LocalDate.MAX);

        assertThrows(ValidationException.class, () -> userController.validateUser(user));
    }

    @Test
    void createEmptyUser() {
        User user1 = new User();

        assertThrows(ValidationException.class, () -> userController.create(user1));
    }
}