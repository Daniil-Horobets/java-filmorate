package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    List<Genre> getAll();
    Genre get(int id);
    void setFilmGenre(Film film);
    List<Genre> loadFilmGenre(int filmId);
}
