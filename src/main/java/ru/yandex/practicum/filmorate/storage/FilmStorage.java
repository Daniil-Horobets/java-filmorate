package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> getAll();
    Film get(int id);
    List<Film> getFilmsByQuery (String query, List<String> by);
    Film create(Film film);
    Film update(Film film);
    void addLike(User user, Film film);
    void deleteLike(User user, Film film);
    List<Film> readBestDirectorFilms(int directorId, String param);
    List<Film> getCommonFilms(int userId, int friendId);
    List<Film> getRecommendations (Integer id);
    boolean delete(int id);

    List<Film> getMostLikedFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year);
}
