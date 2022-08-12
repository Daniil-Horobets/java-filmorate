package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
public class GenreService {

    @Autowired
    private GenreDbStorage genreStorage;

    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    public Genre getById(int id) {
        checkGenreExistence(id, genreStorage);
        return genreStorage.get(id);
    }


    public void checkGenreExistence(int genreId, GenreDbStorage genreStorage) {
        if (genreStorage.getAll().stream().noneMatch(f -> f.getId()==genreId)) {
            throw new NotFoundException("Genre with id=" + genreId + " not found");
        }
    }
}
