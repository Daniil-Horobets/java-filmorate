package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> readAllDirectors() {
        return directorStorage.readAll();
    }

    public Optional<Director> readDirector (int id) {
        return directorStorage.read(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.create(director);
    }

    public boolean updateDirector (Director director) {
        return directorStorage.update(director);
    }

    public boolean deleteDirector(int id) {
        return directorStorage.delete(id);
    }
}
