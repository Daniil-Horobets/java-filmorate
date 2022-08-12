package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Mpa mapToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return new Mpa(resultSet.getInt("MPA_ID"), resultSet.getString("MPA_NAME"));
    }

    public List<Mpa> getAll() {
        final String sqlQuery = "SELECT * FROM MPA ORDER BY MPA_ID";
        return jdbcTemplate.query(sqlQuery, this::mapToMpa);
    }

    public Mpa get(int id) {
        final String sqlQuery = "SELECT * FROM MPA WHERE MPA_ID = ?";
        List<Mpa> mpas = jdbcTemplate.query(sqlQuery, this::mapToMpa, id);
        if (mpas.size() != 1) {
            return null;
        }
        return mpas.get(0);
    }
}
