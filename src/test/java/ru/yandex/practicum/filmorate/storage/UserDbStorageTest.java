package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userDbStorage;

    @Test
    public void testGetAll() {
        List<User> users = userDbStorage.getAll();
        List<User> emptyFilmsList = new ArrayList<>();

        assertEquals(emptyFilmsList, users);
    }

    @Test
    public void testGet() {
        User user = userDbStorage.get(1);
        assertNull(user);
    }
}