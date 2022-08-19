package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FilmStorage {
    List<Film> getAll();
    Film get(int id);
    public List<Film> getFilmsByQuery (String query, List<String> by);
    Film create(Film film);
    Film update(Film film);
    void addLike(User user, Film film);
    void deleteLike(User user, Film film);
    List<Film> getCommonFilms(int userId, int friendId);
}
