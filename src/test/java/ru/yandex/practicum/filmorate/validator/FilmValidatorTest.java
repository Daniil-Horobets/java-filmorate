package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidatorTest {

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
    }

    @Test
    void filmWithBlankNameValidation() {
        film.setName("");

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film));
    }

    @Test
    void filmWithDescriptionLongerThan200CharsValidation() {
        film.setDescription(LONG_DESCRIPTION);

        assertThrows(ValidationException.class,
                () ->FilmValidator.validateFilm(film));
    }

    @Test
    void filmWithReleaseDateBeforeFirstMovieValidation() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film));
    }

    @Test
    void filmWithNegativeDurationValidation() {
        film.setDuration(-1);

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film));
    }


    @Test
    void createEmptyFilm() {
        Film film1 = new Film();

        assertThrows(ValidationException.class,
                () -> FilmValidator.validateFilm(film1));
    }
}