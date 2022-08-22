package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.List;
import java.util.Optional;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final UserService userService;
    private final EventService eventService;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, UserService userService, EventService eventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.userService = userService;
        this.eventService = eventService;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        checkFilmExistence(id, filmStorage);
        return filmStorage.get(id);
    }

    public List<Film> getFilmsByQuery(String query, List<String> by) {
        return filmStorage.getFilmsByQuery(query, by);
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

    public void addMark(int userId, int filmId, Optional <Integer> mark) {
        userService.checkUserExistence(userId, userStorage);
        checkFilmExistence(filmId, filmStorage);
        checkMark(mark);
        filmStorage.setMark(userStorage.get(userId), filmStorage.get(filmId), mark.get());
        eventService.addLikeEvent(userId, filmId);
    }

    public void deleteMark(int userId, int filmId) {
        userService.checkUserExistence(userId, userStorage);
        checkFilmExistence(filmId, filmStorage);
        filmStorage.deleteMark(userId, filmId);
        eventService.removeLikeEvent(userId, filmId);
    }

    public List<Film> getBestFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year) {

        return filmStorage.readBestFilms(count, genreId, year);
    public List<Film> getMostLikedFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        return filmStorage.getMostLikedFilms(count, genreId, year);

    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        userService.checkUserExistence(userId, userStorage);
        userService.checkUserExistence(friendId, userStorage);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void checkFilmExistence(int filmId, FilmStorage filmStorage) {
        Film filmToFind = new Film();
        filmToFind.setId(filmId);
        if (!filmStorage.getAll().contains(filmToFind)) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }
    }

    public void checkMark(Optional<Integer> mark) {
        if(mark.isEmpty()) {
            throw new ValidationException("Mark must be set from 1 to 10, but got " + mark);
        }
        if (mark.get() <= 0 || mark.get() > 10) {
            throw new ValidationException("Mark must be set from 1 to 10, but got " + mark.get());
        }
    }

    public boolean delete(int id) {
        return filmStorage.delete(id);
    }

    public List<Film> readBestDirectorFilms (int directorId, String condition) {
        return filmStorage.readBestDirectorFilms(directorId, condition);
    }
}
