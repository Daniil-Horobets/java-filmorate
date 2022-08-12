package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    @Qualifier("filmDbStorage")
    @Autowired
    private FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    @Autowired
    private UserStorage userStorage;

    @Autowired
    private UserService userService;

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        checkFilmExistence(id, filmStorage);
        return filmStorage.get(id);
    }

    public Film create(Film film) {
        FilmValidator.validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        checkFilmExistence(film.getId(), filmStorage);
        FilmValidator.validateFilm(film);
        return filmStorage.update(film);
    }

    public void addLike(int userId, int filmId) {
        userService.checkUserExistence(userId, userStorage);
        checkFilmExistence(filmId, filmStorage);
        filmStorage.addLike(userStorage.get(userId), filmStorage.get(filmId));
    }

    public void deleteLike(int userId, int filmId) {
        userService.checkUserExistence(userId, userStorage);
        checkFilmExistence(filmId, filmStorage);
        filmStorage.deleteLike(userStorage.get(userId), filmStorage.get(filmId));
    }

    public List<Film> getMostLikedFilms(int count) {
        return filmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikedUsersIds().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void checkFilmExistence(int filmId, FilmStorage filmStorage) {
        if (filmStorage.getAll().stream().noneMatch(f -> f.getId()==filmId)) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }
    }
}
