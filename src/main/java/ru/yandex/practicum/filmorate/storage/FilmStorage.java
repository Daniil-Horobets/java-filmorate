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
    void setMark(User user, Film film, int mark);
    Optional<Double> readFilmRating(int filmId);
    boolean deleteMark(int userId, int filmId);
    List<Film> readBestDirectorFilms(int directorId, String param);
    List<Film> readBestFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year);
    List<Film> getCommonFilms(int userId, int friendId);
    List<Film> getRecommendations (Integer id);
    boolean delete(int id);
    void updateFilmRatings(int userId);

    List<Film> getMostLikedFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year);
}
