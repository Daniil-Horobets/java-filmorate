package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import resources.EntitiesForTests;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor (onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
class FilmDbStorageTest {

    private final FilmService filmService;
    private final UserDbStorage userDbStorage;
    private final DirectorStorage directorStorage;
    private final FilmStorage filmStorage;
    private final Film testFilm = EntitiesForTests.getTestFilm();
    private final Film testFilm2 = EntitiesForTests.getTestFilm2();
    private final Film testFilm3 = EntitiesForTests.getTestFilm3();
    private final Film testFilm4 = EntitiesForTests.getTestFilm4();
    private final Film testFilm5 = EntitiesForTests.getTestFilm5();
    private final User testUser = EntitiesForTests.getTestUser();
    private final User testUser2 = EntitiesForTests.getTestUser2();
    private final User testUser3 = EntitiesForTests.getTestUser3();
    private final Director testDirector = EntitiesForTests.getTestDirector();
    private final Director testDirector2 = EntitiesForTests.getTestDirector2();

    @Test
    @Order(1)
    public void testGetAll() {
        List<Film> films = filmService.getAll();
        List<Film> emptyFilmsList = new ArrayList<>();

        assertEquals(emptyFilmsList, films);
    }

    @Test
    @Order(2)
    public void testGet() {
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> filmService.getById(1));
        assertTrue(thrown.getMessage().contains("Film with id=1 not found"));
    }

    @Test
    @Order(3)
    public void testCreate() {
        directorStorage.create(testDirector);
        directorStorage.create(testDirector2);
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
    @Order(5)
    public void testAddMarks() {
        filmService.create(testFilm2);
        filmService.create(testFilm3);
        filmService.create(testFilm4);
        filmService.create(testFilm5);
        userDbStorage.create(testUser);
        userDbStorage.create(testUser2);
        userDbStorage.create(testUser3);
        filmService.addMark(testUser.getId(),testFilm.getId(), Optional.of(8));
        filmService.addMark(testUser.getId(),testFilm3.getId(), Optional.of(9));
        filmService.addMark(testUser.getId(),testFilm2.getId(), Optional.of(6));
        filmService.addMark(testUser.getId(),testFilm5.getId(), Optional.of(6));
        filmService.addMark(testUser2.getId(),testFilm.getId(), Optional.of(5));
        filmService.addMark(testUser2.getId(),testFilm3.getId(), Optional.of(8));
        filmService.addMark(testUser2.getId(),testFilm2.getId(), Optional.of(7));
        filmService.addMark(testUser2.getId(),testFilm5.getId(), Optional.of(7));
        filmService.addMark(testUser3.getId(),testFilm.getId(), Optional.of(6));
        filmService.addMark(testUser3.getId(),testFilm3.getId(), Optional.of(7));
        filmService.addMark(testUser3.getId(),testFilm2.getId(), Optional.of(6));
        filmService.addMark(testUser3.getId(),testFilm5.getId(), Optional.of(10));

        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.empty());

        assertEquals(5, bestFilms.size());
        assertEquals(3, bestFilms.get(0).getId());
        assertEquals(5, bestFilms.get(1).getId());
        assertEquals(1, bestFilms.get(2).getId());
        assertEquals(2, bestFilms.get(3).getId());
        assertEquals(4, bestFilms.get(4).getId());
    }

    @Test
    @Order(6)
    public void testChangeMark() {
        assertEquals(8, Double.parseDouble(filmService.getById(3).getUserRating()));
        filmService.addMark(testUser.getId(),testFilm3.getId(), Optional.of(1));
        filmService.addMark(testUser2.getId(),testFilm3.getId(), Optional.of(1));
        filmService.addMark(testUser3.getId(),testFilm3.getId(), Optional.of(1));
        assertEquals(1, Double.parseDouble(filmService.getById(3).getUserRating()));
    }

    @Test
    @Order(7)
    public void testBestFilmsSortByGenre() {
        List<Film> bestFilms = filmService.getBestFilms(10, Optional.of(1), Optional.empty());

        assertEquals(3, bestFilms.size());
        assertEquals(5, bestFilms.get(0).getId());
        assertEquals(1, bestFilms.get(1).getId());
        assertEquals(3, bestFilms.get(2).getId());
    }

    @Test
    @Order(8)
    public void testBestFilmsSortByYear() {

        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.of(2008));

            assertEquals(3, bestFilms.size());
            assertEquals(5, bestFilms.get(0).getId());
            assertEquals(3, bestFilms.get(1).getId());
            assertEquals(4, bestFilms.get(2).getId());
    }

    @Test
    @Order(9)
    public void testBestFilmsSortByYearAndGenre() {

        assertEquals(1, filmService.getBestFilms(10, Optional.of(1), Optional.of(1998)).get(0).getId());
    }

    @Test
    @Order(10)
    public void testBestFilmsSortByDirectorRating() {

        List<Film> bestDirectorFilms = filmStorage.readBestDirectorFilms(1, "likes");

        assertEquals(3, bestDirectorFilms.size());
        assertEquals(5, bestDirectorFilms.get(0).getId());
        assertEquals(1, bestDirectorFilms.get(1).getId());
        assertEquals(2, bestDirectorFilms.get(2).getId());
    }

    @Test
    @Order(11)
    public void testBestFilmsSortByDirectorYear() {

        List<Film> bestDirectorFilms = filmStorage.readBestDirectorFilms(1, "year");

        assertEquals(3, bestDirectorFilms.size());
        assertEquals(2, bestDirectorFilms.get(0).getId());
        assertEquals(1, bestDirectorFilms.get(1).getId());
        assertEquals(5, bestDirectorFilms.get(2).getId());
    }

    @Test
    @Order(12)
    public void testCommonFilms() {

        List<Film> commonFilms = filmStorage.getCommonFilms(1, 2);

        assertEquals(4, commonFilms.size());
        assertEquals(5, commonFilms.get(0).getId());
        assertEquals(1, commonFilms.get(1).getId());
        assertEquals(2, commonFilms.get(2).getId());
        assertEquals(3, commonFilms.get(3).getId());
    }

    @Test
    @Order(13)
    public void testDeleteMarks() {

        filmService.deleteMark(testUser.getId(), testFilm.getId());
        filmService.deleteMark(testUser2.getId(), testFilm.getId());
        filmService.deleteMark(testUser2.getId(), testFilm3.getId());
        filmService.deleteMark(testUser3.getId(), testFilm.getId());
        filmService.deleteMark(testUser3.getId(), testFilm3.getId());
        filmService.deleteMark(testUser3.getId(), testFilm2.getId());

        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.empty());

        assertEquals(5, bestFilms.size());
        assertEquals(5, bestFilms.get(0).getId());
        assertEquals(2, bestFilms.get(1).getId());
        assertEquals(3, bestFilms.get(2).getId());
        assertEquals(1, bestFilms.get(3).getId());
        assertEquals(4, bestFilms.get(4).getId());
    }

    @Test
    @Order(14)
    public void testCommonFilmsAfterMarksDeleted() {

        List<Film> commonFilms = filmStorage.getCommonFilms(1, 3);

        assertEquals(1, commonFilms.size());
        assertEquals(5, commonFilms.get(0).getId());
    }

    @Test
    @Order(15)
    public void testGetRecommendations() {

        List<Film> recommendedFilms = filmStorage.getRecommendations( 3);

        assertEquals(1, recommendedFilms.size());
        assertEquals(2, recommendedFilms.get(0).getId());
    }

    @Test
    @Order(16)
    public void testGetRecommendationsMarkChanged() {

        filmService.addMark(testUser.getId(),testFilm.getId(), Optional.of(8));

        List<Film> recommendedFilms = filmStorage.getRecommendations( 3);

        assertEquals(2, recommendedFilms.size());
        assertEquals(2, recommendedFilms.get(0).getId());
        assertEquals(1, recommendedFilms.get(1).getId());
    }
    @Test
    @Order(17)
    public void testBestFilmsSortByGenreNoGenre() {
        List<Film> bestFilms = filmService.getBestFilms(10, Optional.of(10), Optional.empty());
        assertTrue(bestFilms.isEmpty());
    }

    @Test
    @Order(18)
    public void testBestFilmsSortByYearNoYear() {

        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.of(2028));
        assertTrue(bestFilms.isEmpty());

    }

    @Test
    @Order(19)
    public void testBestFilmsSortByMarksNoMarks() {

        filmService.deleteMark(testUser.getId(), testFilm3.getId());
        filmService.deleteMark(testUser.getId(), testFilm2.getId());
        filmService.deleteMark(testUser.getId(), testFilm5.getId());
        filmService.deleteMark(testUser2.getId(), testFilm2.getId());
        filmService.deleteMark(testUser2.getId(), testFilm5.getId());
        filmService.deleteMark(testUser3.getId(), testFilm5.getId());

        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.empty());

        assertEquals(5, bestFilms.size());
        assertEquals(1, bestFilms.get(0).getId());
        assertEquals(2, bestFilms.get(1).getId());
        assertEquals(3, bestFilms.get(2).getId());
        assertEquals(4, bestFilms.get(3).getId());
        assertEquals(5, bestFilms.get(4).getId());
    }

    @Test
    @Order(20)
    public void testSearchFilmByDirector() {

        filmService.addMark(testUser.getId(),testFilm.getId(), Optional.of(8));
        filmService.addMark(testUser.getId(),testFilm3.getId(), Optional.of(9));
        filmService.addMark(testUser.getId(),testFilm2.getId(), Optional.of(6));
        filmService.addMark(testUser.getId(),testFilm5.getId(), Optional.of(6));
        filmService.addMark(testUser2.getId(),testFilm.getId(), Optional.of(5));
        filmService.addMark(testUser2.getId(),testFilm3.getId(), Optional.of(8));
        filmService.addMark(testUser2.getId(),testFilm2.getId(), Optional.of(7));
        filmService.addMark(testUser2.getId(),testFilm5.getId(), Optional.of(7));
        filmService.addMark(testUser3.getId(),testFilm.getId(), Optional.of(6));
        filmService.addMark(testUser3.getId(),testFilm3.getId(), Optional.of(7));
        filmService.addMark(testUser3.getId(),testFilm2.getId(), Optional.of(6));
        filmService.addMark(testUser3.getId(),testFilm5.getId(), Optional.of(10));

        List<Film> SearchFilms = filmService.getFilmsByQuery("Name1", new ArrayList<>(List.of("director")));

        assertEquals(3, SearchFilms.size());
        assertEquals(5, SearchFilms.get(0).getId());
        assertEquals(1, SearchFilms.get(1).getId());
        assertEquals(2, SearchFilms.get(2).getId());
    }

    @Test
    @Order(21)
    public void testSearchFilmByTitle() {

        List<Film> SearchFilms = filmService.getFilmsByQuery("FilmName5", new ArrayList<>(List.of("title")));

        assertEquals(1, SearchFilms.size());
        assertEquals(5, SearchFilms.get(0).getId());

    }

    @Test
    @Order(22)
    public void testSearchFilmByTitleAndDirector() {

        List<Film> SearchFilms = filmService.getFilmsByQuery("Name2", new ArrayList<>(List.of("title", "director")));

        assertEquals(3, SearchFilms.size());
        assertEquals(4, SearchFilms.get(0).getId());
        assertEquals(3, SearchFilms.get(1).getId());
        assertEquals(2, SearchFilms.get(2).getId());

    }

    @Test
    @Order(23)
    public void testSearchFilmByNoTitleAndNoDirector() {

        List<Film> SearchFilms = filmService.getFilmsByQuery("1emaN", new ArrayList<>(List.of("title", "director")));

        for(Film film : SearchFilms) {
            System.out.println(film);
        }

        assertEquals(0, SearchFilms.size());

    }
    @Test
    @Order(24)
    public void testDeleteUserDeleteMarks() {

        userDbStorage.delete(1);
        userDbStorage.delete(3);

        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.empty());

        assertEquals(5, bestFilms.size());
        assertEquals(3, bestFilms.get(0).getId());
        assertEquals(2, bestFilms.get(1).getId());
        assertEquals(5, bestFilms.get(2).getId());
        assertEquals(1, bestFilms.get(3).getId());
        assertEquals(4, bestFilms.get(4).getId());
    }

    @Test
    @Order(25)
    public void testDeleteFilmDeleteMarks() {

        filmService.delete(3);
        filmService.delete(2);


        List<Film> bestFilms = filmService.getBestFilms(10, Optional.empty(), Optional.empty());

        assertEquals(3, bestFilms.size());
        assertEquals(5, bestFilms.get(0).getId());
        assertEquals(1, bestFilms.get(1).getId());
        assertEquals(4, bestFilms.get(2).getId());
    }
}