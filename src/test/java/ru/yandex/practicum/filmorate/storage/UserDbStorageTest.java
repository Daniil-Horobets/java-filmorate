package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import resources.EntitiesForTests;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final User testUser = EntitiesForTests.getTestUser();
    private final Film testFilm = EntitiesForTests.getTestFilm();
    private final User testFriend = EntitiesForTests.getTestFriend();

    @Test
    @Order(1)
    public void testGetAll() {
        List<User> users = userDbStorage.getAll();

        List<User> emptyFilmsList = new ArrayList<>();

        assertEquals(emptyFilmsList, users);
    }

    @Test
    @Order(2)
    public void testGet() {
        User user = userDbStorage.get(1);
        assertNull(user);
    }

    @Test
    @Order(3)
    public void testCreate() {
        userDbStorage.create(testUser);
        User createdUser = userDbStorage.get(testUser.getId());
        assertEquals(testUser, createdUser);
    }

    @Test
    @Order(4)
    public void testUpdate() {
        User userToUpdate = testUser;
        userToUpdate.setName("Updated Name");
        userDbStorage.update(userToUpdate);
        User updatedUser = userDbStorage.get(userToUpdate.getId());

        assertEquals(userToUpdate, updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
    }

    @Test
    @Order(5)
    public void testAddAndGetFriend() {
        userDbStorage.create(testFriend);
        userDbStorage.addFriend(testUser,testFriend);
        List<User> usersFriends = userDbStorage.getFriends(testUser.getId());

        assertEquals(List.of(testFriend), usersFriends);
    }

    @Test
    @Order(6)
    public void testDeleteFriend() {
        userDbStorage.deleteFriend(testUser,testFriend);
        User filmWithoutFriend = userDbStorage.get(testUser.getId());

        assertTrue(filmWithoutFriend.getFriendsIds().isEmpty());
    }
}