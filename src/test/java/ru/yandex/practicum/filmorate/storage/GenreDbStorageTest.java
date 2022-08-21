package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import resources.EntitiesForTests;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    private final FilmDbStorage filmDbStorage;

    private final Film testFilm = EntitiesForTests.getTestFilm();

    @Test
    public void testGetAll() {
        List<Genre> genres = genreDbStorage.getAll();

        assertEquals(6, genres.size());
        assertEquals("Комедия", genres.get(0).getName());
        assertEquals("Драма", genres.get(1).getName());
        assertEquals("Мультфильм", genres.get(2).getName());
        assertEquals("Триллер", genres.get(3).getName());
        assertEquals("Документальный", genres.get(4).getName());
        assertEquals("Боевик", genres.get(5).getName());

    }

    @Test
    public void testGet() {
        Genre genre = genreDbStorage.get(1);

        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    public void testSetFilmGenre() {
        testFilm.setGenres(Set.of(genreDbStorage.get(1)));
        filmDbStorage.create(testFilm);
        genreDbStorage.setFilmGenre(testFilm);

        assertEquals(Set.of(genreDbStorage.get(1)), filmDbStorage.get(testFilm.getId()).getGenres());
    }

    @Test
    public void testLoadFilmGenre() {
        testFilm.setGenres(Set.of(genreDbStorage.get(3)));
        genreDbStorage.loadFilmGenre(testFilm);

        for (Genre genre : testFilm.getGenres()) {
            System.out.println(genre.getId());
            System.out.println(genre.getName());
        }

        assertNotEquals(Set.of(genreDbStorage.get(3)), testFilm.getGenres());
        assertEquals(filmDbStorage.get(testFilm.getId()).getGenres(), testFilm.getGenres());
    }
}