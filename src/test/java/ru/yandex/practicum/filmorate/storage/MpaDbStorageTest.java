package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
class MpaDbStorageTest {
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void testGet() {
        Mpa mpa = mpaDbStorage.get(1);

        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
    }

    @Test
    public void testGetAll() {
        List<Mpa> mpas = mpaDbStorage.getAll();

        assertEquals(5, mpas.size());
        assertEquals("G", mpas.get(0).getName());
        assertEquals("PG", mpas.get(1).getName());
        assertEquals("PG-13", mpas.get(2).getName());
        assertEquals("R", mpas.get(3).getName());
        assertEquals("NC-17", mpas.get(4).getName());

    }

}