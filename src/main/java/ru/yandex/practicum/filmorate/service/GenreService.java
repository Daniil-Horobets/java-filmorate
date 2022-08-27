package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public GenreService(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    public List<Genre> getAll() {
        return genreDbStorage.getAll();
    }

    public Genre getById(int id) {
        checkGenreExistence(id, genreDbStorage);
        return genreDbStorage.get(id);
    }


    public void checkGenreExistence(int genreId, GenreDbStorage genreStorage) {
        Genre genreToFind = new Genre();
        genreToFind.setId(genreId);
        if (!genreStorage.getAll().contains(genreToFind)) {
            throw new NotFoundException("Genre with id=" + genreId + " not found");
        }
    }
}
