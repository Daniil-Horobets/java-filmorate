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
//@RequiredArgsConstructor //(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
class FilmDbStorageTest {
    @Autowired
    FilmDbStorage filmDbStorage;
    @Autowired
    UserDbStorage userDbStorage;
    private final Film testFilm = EntitiesForTests.getTestFilm();
    private final User testUser = EntitiesForTests.getTestUser();

    @Test
    @Order(1)
    public void testGetAll() {
        List<Film> films = filmDbStorage.getAll();
        List<Film> emptyFilmsList = new ArrayList<>();

        assertEquals(emptyFilmsList, films);
    }

    @Test
    @Order(2)
    public void testGet() {
        Film film = filmDbStorage.get(1);
        assertNull(film);
    }

    @Test
    @Order(3)
    public void testCreate() {
        filmDbStorage.create(testFilm);
        Film createdFilm = filmDbStorage.get(testFilm.getId());
        assertEquals(testFilm, createdFilm);
    }

    @Test
    @Order(4)
    public void testUpdate() {
        Film filmToUpdate = testFilm;
        filmToUpdate.setName("Updated Name");
        filmDbStorage.update(filmToUpdate);
        Film updatedFilm = filmDbStorage.get(filmToUpdate.getId());

        assertEquals(filmToUpdate, updatedFilm);
    }

    @Test
    @Order(5)
    public void testAddLike() {
        User secondTestUser = testUser;
        secondTestUser.setId(3);
        secondTestUser.setEmail("stu@mail");
        secondTestUser.setLogin("stuLogin");

        userDbStorage.create(secondTestUser);
        filmDbStorage.addLike(secondTestUser,testFilm);
        Film filmWithLike = filmDbStorage.get(testFilm.getId());

        assertTrue(filmWithLike.getLikedUsersIds().contains(secondTestUser.getId()));
    }

    @Test
    @Order(6)
    public void testDeleteLike() {
        filmDbStorage.deleteLike(testUser,testFilm);
        Film filmWithoutLike = filmDbStorage.get(testFilm.getId());

        assertTrue(filmWithoutLike.getLikedUsersIds().isEmpty());
    }
}