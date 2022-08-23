package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.IDirectorRepository;

import java.util.Collection;
import java.util.Optional;

@Service
public class DirectorService {

    private final IDirectorRepository iDirectorRepository;

    @Autowired
    public DirectorService(IDirectorRepository iDirectorRepository) {
        this.iDirectorRepository = iDirectorRepository;
    }

    public Collection<Director> readAllDirectors() {
        return iDirectorRepository.readAll();
    }

    public Optional<Director> readDirector (int id) {
        return iDirectorRepository.read(id);
    }

    public Director createDirector(Director director) {
        return iDirectorRepository.create(director);
    }

    public boolean updateDirector (Director director) {
        return iDirectorRepository.update(director);
    }

    public boolean deleteDirector(int id) {
        return iDirectorRepository.delete(id);
    }
}
