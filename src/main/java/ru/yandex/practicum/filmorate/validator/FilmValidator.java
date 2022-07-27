package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;

@Slf4j
public class FilmValidator {
    public static void validateFilm(Film film, HashMap<Integer, Film> films, RequestMethod method) {
        if (!films.containsKey(film.getId()) && method.equals(RequestMethod.PUT)) {
            log.error("ValidationException: {}", "Film with id: " + film.getId() + " does not exist");
            throw new ValidationException("Film with id: " + film.getId() + " does not exist");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("ValidationException: {}", "Film name shouldn't not be blank");
            throw new ValidationException("Film name shouldn't not be blank");
        }
        if (film.getDescription().length() > 200) {
            log.error("ValidationException: {}", "Film description shouldn't be longer than 200 characters");
            throw new ValidationException("Film description shouldn't be longer than 200 characters");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("ValidationException: {}", "Film release date should be before 28 Dec 1895");
            throw new ValidationException("Film release date should be before 28 Dec 1895");
        }
        if (film.getDuration() < 0) {
            log.error("ValidationException: {}", "Film duration should be positive");
            throw new ValidationException("Film duration should be positive");
        }
    }
}
