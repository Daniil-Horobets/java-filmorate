package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    private final HashMap<Integer, User> usersMap = new HashMap<>();
    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("email@mail.com");
        user.setName("Name");
        user.setBirthday(LocalDate.now());
        user.setLogin("Login");
        usersMap.put(1, user);
    }

    @Test
    void userWithBlankEmailValidation() {
        user.setEmail("");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user, usersMap, RequestMethod.PUT));
    }

    @Test
    void userWithEmailWithoutAtSymbolValidation() {
        user.setEmail("email.com");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user, usersMap, RequestMethod.PUT));
    }

    @Test
    void userWithBlankOrContainingSpacesLoginValidation() {
        user.setLogin("");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user, usersMap, RequestMethod.PUT));

        user.setLogin("Log in");

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user, usersMap, RequestMethod.PUT));
    }

    @Test
    void userWithBirthdayAfterNowValidation() {
        user.setBirthday(LocalDate.MAX);

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user, usersMap, RequestMethod.PUT));
    }

    @Test
    void createEmptyUser() {
        User user1 = new User();
        usersMap.put(2, user1);

        assertThrows(ValidationException.class,
                () -> UserValidator.validateUser(user, usersMap, RequestMethod.PUT));
    }
}