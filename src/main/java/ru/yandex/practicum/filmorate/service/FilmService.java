package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    @Autowired
    private InMemoryFilmStorage inMemoryFilmStorage;

    @Autowired
    private InMemoryUserStorage inMemoryUserStorage;

    @Autowired
    private UserService userService;

    public List<Film> getAll() {
        return inMemoryFilmStorage.getAll();
    }

    public Film getById(int id) {
        checkFilmExistence(id, inMemoryFilmStorage);
        return inMemoryFilmStorage.get(id);
    }

    public Film create(Film film) {
        FilmValidator.validateFilm(film);
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film film) {
        checkFilmExistence(film.getId(), inMemoryFilmStorage);
        FilmValidator.validateFilm(film);
        return inMemoryFilmStorage.update(film);
    }

    public void addLike(int userId, int filmId) {
        userService.checkUserExistence(userId, inMemoryUserStorage);
        checkFilmExistence(filmId, inMemoryFilmStorage);
        inMemoryFilmStorage.addLike(inMemoryUserStorage.get(userId), inMemoryFilmStorage.get(filmId));
    }

    public void deleteLike(int userId, int filmId) {
        userService.checkUserExistence(userId, inMemoryUserStorage);
        checkFilmExistence(filmId, inMemoryFilmStorage);
        inMemoryFilmStorage.deleteLike(inMemoryUserStorage.get(userId), inMemoryFilmStorage.get(filmId));
    }

    public List<Film> getMostLikedFilms(int count) {
        return inMemoryFilmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikedUsersIds().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void checkFilmExistence(int filmId, InMemoryFilmStorage inMemoryFilmStorage) {
        if (!inMemoryFilmStorage.getFilms().containsKey(filmId)) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }
    }
}
