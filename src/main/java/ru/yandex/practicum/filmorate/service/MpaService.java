package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@Service
public class MpaService {

    @Autowired
    private MpaDbStorage mpaStorage;

    public List<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    public Mpa getById(int id) {
        checkMpaExistence(id, mpaStorage);
        return mpaStorage.get(id);
    }

    public void checkMpaExistence(int mpaId, MpaDbStorage mpaStorage) {
        Mpa mpaToFind = new Mpa();
        mpaToFind.setId(mpaId);
        if (!mpaStorage.getAll().contains(mpaToFind)) {
            throw new NotFoundException("MPA with id=" + mpaId + " not found");
        }
    }
}
