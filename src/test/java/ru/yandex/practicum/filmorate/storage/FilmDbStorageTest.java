package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;

    @Test
    public void testGetAll() {
        List<Film> films = filmDbStorage.getAll();
        List<Film> emptyFilmsList = new ArrayList<>();

        assertEquals(emptyFilmsList, films);
    }

    @Test
    public void testGet() {
        Film film = filmDbStorage.get(1);
        assertNull(film);
    }
}