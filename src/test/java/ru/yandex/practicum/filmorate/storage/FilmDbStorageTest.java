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
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor (onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
class FilmDbStorageTest {
    @Autowired
    FilmService filmService;
    @Autowired
    UserDbStorage userDbStorage;
    private final Film testFilm = EntitiesForTests.getTestFilm();
    private final User testUser = EntitiesForTests.getTestUser();
    private final User testFriend = EntitiesForTests.getTestFriend();

    @Test
    @Order(1)
    public void testGetAll() {
        List<Film> films = filmService.getAll();
        List<Film> emptyFilmsList = new ArrayList<>();

        assertEquals(emptyFilmsList, films);
    }

    @Test
    @Order(2)
    public void testGetNoFilmThrowsNotFoundException() {
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> filmService.getById(1));
        assertTrue(thrown.getMessage().contains("Film with id=1 not found"));
    }

    @Test
    @Order(3)
    public void testCreate() {
        filmService.create(testFilm);
        Film createdFilm = filmService.getById(testFilm.getId());
        assertEquals(testFilm, createdFilm);
    }

    @Test
    @Order(4)
    public void testUpdate() {
        Film filmToUpdate = testFilm;
        filmToUpdate.setName("Updated Name");
        filmService.update(filmToUpdate);
        Film updatedFilm = filmService.getById(filmToUpdate.getId());

        assertEquals(filmToUpdate, updatedFilm);
    }

    @Test
    public void testGetCommonFilms() {
        filmService.create(testFilm);
        testUser.setEmail("stu1@mail");
        testUser.setLogin("stu2Login");
        userDbStorage.create(testUser);
        testFriend.setEmail("stu2@mail");
        testFriend.setLogin("stu2@mail");
        userDbStorage.create(testFriend);
        filmService.addLike(testUser.getId(), testFilm.getId());
        filmService.addLike(testFriend.getId(), testFilm.getId());
        assertEquals(testFilm, filmService.getCommonFilms(testUser.getId(), testFriend.getId()).get(0),
                "Выборка 'Общие фильмы' (из одного элемента)  не совпадает с исходной");
    }
}