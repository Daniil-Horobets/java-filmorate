package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    @Autowired
    private FilmService filmService;

    @GetMapping
    public List<Film> findAll() {
        log.info("Request endpoint: 'GET /films'");
        return filmService.getAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Request endpoint: 'POST /films'");
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Request endpoint: 'PUT /films'");
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.info("Request endpoint: 'GET /films/{}'", id);
        return filmService.getById(id);
    }

    @GetMapping("/search")
    public List<Film> getFilmsByQuery (@RequestParam String query,
                                       @RequestParam List<String> by) {
        log.info("Request endpoint: 'GET /films/search?query={}'", query);
        return filmService.getFilmsByQuery(query, by);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike (@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'PUT /films/{}/like/{}'", id, userId);
        filmService.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike (@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'DELETE /films/{}/like/{}'", id, userId);
        filmService.deleteLike(userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getMostLikedFilms(
            @RequestParam(defaultValue = "10", name = "count") Integer count,
            @RequestParam(required = false, name = "genreId") Integer genreId,
            @RequestParam(required = false, name = "year") Integer year) {
        log.info("Request endpoint: 'GET /films/popular?count={}&genreId={}&year={}'", count, genreId, year);
        return filmService.getMostLikedFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        log.info("Request endpoint: 'GET /common?userId={}&friendId={}'", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}
