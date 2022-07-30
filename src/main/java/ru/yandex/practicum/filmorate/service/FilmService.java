package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    @Autowired
    InMemoryFilmStorage inMemoryFilmStorage;

    @Autowired
    InMemoryUserStorage inMemoryUserStorage;

    public List<Film> getAll() {
        return inMemoryFilmStorage.getAll();
    }

    public Film get(int id) {
        if (!inMemoryFilmStorage.getFilms().containsKey(id)) {
            throw new NotFoundException("Film with id=" + id + " not found");
        }
        return inMemoryFilmStorage.get(id);
    }

    public Film create(Film film) {
        FilmValidator.validateFilm(film);
        return inMemoryFilmStorage.create(film);
    }

    public Film update(Film film) {
        if (!inMemoryFilmStorage.getFilms().containsKey(film.getId())) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found");
        }
        FilmValidator.validateFilm(film);
        return inMemoryFilmStorage.update(film);
    }

    public void addLike(int userId, int filmId) {
        User user = inMemoryUserStorage.get(userId);
        Film film = inMemoryFilmStorage.get(filmId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        if (film == null) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }
        inMemoryFilmStorage.addLike(user, film);
    }

    public void deleteLike(int userId, int filmId) {
        User user = inMemoryUserStorage.get(userId);
        Film film = inMemoryFilmStorage.get(filmId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        if (film == null) {
            throw new NotFoundException("Film with id=" + filmId + " not found");
        }
        inMemoryFilmStorage.deleteLike(user, film);
    }

    public List<Film> getMostLikedFilms(int count) {
        return inMemoryFilmStorage.getAll()
                .stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikedUsersIds().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
