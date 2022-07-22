package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

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
        FilmValidator.validateFilm(film, films, RequestMethod.POST);
        idCounter++;
        film.setId(idCounter);
        films.put(idCounter, film);
        return film;
    }

    // Обновление фильма
    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Request endpoint: 'PUT /films'");
        FilmValidator.validateFilm(film, films, RequestMethod.PUT);
        films.put(film.getId(), film);
        return film;
    }
}
