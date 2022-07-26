package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidatorTest {

    private final HashMap<Integer, Film> filmsMap = new HashMap<>();
    private Film film;
    private static final String LONG_DESCRIPTION = "This text is longer than 200 chars. This text is longer than 200 "
            + "chars. This text is longer than 200 chars. This text is longer than 200 chars. This text is longer than "
            + "200 chars. This text is longer than 200 chars. This text is longer than 200 chars.";

    @BeforeEach
    void init() {
        film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);
        filmsMap.put(1, film);
    }

    @Test
    void filmWithBlankNameValidation() {
        film.setName("");

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film, filmsMap, RequestMethod.PUT));
    }

    @Test
    void filmWithDescriptionLongerThan200CharsValidation() {
        film.setDescription(LONG_DESCRIPTION);

        assertThrows(ValidationException.class,
                () ->FilmValidator.validateFilm(film, filmsMap, RequestMethod.PUT));
    }

    @Test
    void filmWithReleaseDateBeforeFirstMovieValidation() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film, filmsMap, RequestMethod.PUT));
    }

    @Test
    void filmWithNegativeDurationValidation() {
        film.setDuration(-1);

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film, filmsMap, RequestMethod.PUT));
    }


    @Test
    void createEmptyFilm() {
        Film film1 = new Film();
        filmsMap.put(2, film1);

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film1, filmsMap, RequestMethod.PUT));
    }
}