package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.IDirectorRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class DirectorController {

    private final IDirectorRepository iDirectorRepository;
    private final FilmStorage filmStorage;

    @Autowired
    public DirectorController(IDirectorRepository iDirectorRepository, FilmStorage filmStorage) {
        this.iDirectorRepository = iDirectorRepository;
        this.filmStorage = filmStorage;
    }

    @GetMapping(value = "/directors")
    public ResponseEntity<Collection<Director>> readAllDirectors() {
        final Collection<Director> directors = iDirectorRepository.readAll();
        log.debug("Текущее количество режиссеров: {}", directors.size());
        return directors != null
                ? new ResponseEntity<>(directors, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/directors/{id}")
    public ResponseEntity<Director> readDirector (@PathVariable(name = "id") int id) {
        final Optional<Director> director = iDirectorRepository.read(id);

        return !director.isEmpty()
                ? new ResponseEntity<>(director.get(), HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/directors")
    public ResponseEntity<Director> createDirector(@RequestBody Director director) {

        final Director newDirector = iDirectorRepository.create(director);
        log.debug(String.valueOf(newDirector));
        return newDirector != null
                ? new ResponseEntity<>(newDirector, HttpStatus.CREATED)
                : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/directors")
    public ResponseEntity<Director> updateDirector (@RequestBody Director director) {

        final boolean updated = iDirectorRepository.update(director);
        log.debug(String.valueOf(iDirectorRepository.read(director.getId())));
        return updated
                ? new ResponseEntity<>(iDirectorRepository.read(director.getId()).get(), HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/directors/{id}")
    public ResponseEntity<?> deleteDirector(@PathVariable(name = "id") int id) {
        final boolean deleted = iDirectorRepository.delete(id);

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    @GetMapping(value = "/films/director/{directorId}")
    public ResponseEntity <List<Film>> readBestDirectorFilms (@PathVariable(name = "directorId") int directorId,
                                                              @RequestParam (value = "sortBy", required = true) String condition) {
        final List<Film> directorFilms = filmStorage.readBestDirectorFilms(directorId, condition);

        return directorFilms != null
                ? new ResponseEntity<>(directorFilms, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
