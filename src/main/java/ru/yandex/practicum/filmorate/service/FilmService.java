package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.List;
import java.util.Optional;

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

    @Autowired
    private EventService eventService;

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

    public void addLike(int userId, int filmId) {
        userService.checkUserExistence(userId, userStorage);
        checkFilmExistence(filmId, filmStorage);
        filmStorage.addLike(userStorage.get(userId), filmStorage.get(filmId));
        eventService.addLikeEvent(userId, filmId);
    }

    public void deleteLike(int userId, int filmId) {
        userService.checkUserExistence(userId, userStorage);
        checkFilmExistence(filmId, filmStorage);
        filmStorage.deleteLike(userStorage.get(userId), filmStorage.get(filmId));
        eventService.removeLikeEvent(userId, filmId);
    }

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

    public boolean delete(int id) {
        return filmStorage.delete(id);
    }

    public List<Film> readBestDirectorFilms (int directorId, String condition) {
        return filmStorage.readBestDirectorFilms(directorId, condition);
    }
}
