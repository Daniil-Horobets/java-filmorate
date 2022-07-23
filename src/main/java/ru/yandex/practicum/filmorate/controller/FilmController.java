package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private final HashMap<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    // Получение всех фильмов.
    @GetMapping
    public List<Film> findAll() {
        log.info("Request endpoint: 'GET /films'");
        return new ArrayList<>(films.values());
    }

    // Добавление фильма
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Request endpoint: 'POST /films'");
        validateFilm(film);
        idCounter++;
        film.setId(idCounter);
        films.put(idCounter, film);
        return film;
    }

    // Обновление фильма
    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Request endpoint: 'PUT /films'");
        if (!films.containsKey(film.getId())) {
            String exceptionMessage = "Film with id: " + film.getId() + "does not exist";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        validateFilm(film);
        films.put(film.getId(), film);
        return film;
    }

    protected void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            String exceptionMessage = "Film name shouldn't not be blank";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (film.getDescription().length() > 200) {
            String exceptionMessage = "Film description shouldn't be longer than 200 characters";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            String exceptionMessage = "Film release date should be before 28 Dec 1895";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
        if (film.getDuration() < 0) {
            String exceptionMessage = "Film duration should be positive";
            log.error("ValidationException: " + exceptionMessage);
            throw new ValidationException(exceptionMessage);
        }
    }
}
