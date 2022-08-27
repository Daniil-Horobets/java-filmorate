package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Optional;

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

    @PutMapping("/{filmId}/mark/{userId}")
    public void addMark (@PathVariable int filmId,
                         @PathVariable int userId,
                         @RequestParam(value = "mark", required = true) Optional<Integer> mark) {

        log.info("Request endpoint: 'PUT /films/{}/mark/{}?mark={}'", filmId, userId, mark);
        filmService.addMark(userId, filmId, mark);
    }

    @GetMapping("/search")
    public List<Film> getFilmsByQuery (@RequestParam String query,
                                       @RequestParam List<String> by) {
        log.info("Request endpoint: 'GET /films/search?query={}'", query);
        return filmService.getFilmsByQuery(query, by);
    }

    @DeleteMapping("/{filmId}/mark/{userId}")
    public void deleteMark (@PathVariable int filmId, @PathVariable int userId) {
        log.info("Request endpoint: 'DELETE /films/{}/like/{}'", filmId, userId);
        filmService.deleteMark(userId, filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'PUT /films/{}/like/{}'", id, userId);
        filmService.addLike(userId, id);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Request endpoint: 'DELETE /films/{}/like/{}'", filmId, userId);
        filmService.deleteMark(userId, filmId);
    }

    @GetMapping("/popular")
    public List<Film> getBestFilms(
            @RequestParam(defaultValue = "10", name = "count") Integer count,
            @RequestParam(required = false, name = "genreId") Optional<Integer> genreId,
            @RequestParam(required = false, name = "year") Optional<Integer> year) {
        log.info("Request endpoint: 'GET /films/popular?count={}&genreId={}&year={}'", count, genreId, year);
        return filmService.getBestFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        log.info("Request endpoint: 'GET /common?userId={}&friendId={}'", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") int id) {
        final boolean deleted = filmService.delete(id);
        log.info("Request endpoint: 'DELETE /films/{}'", id);
        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
