package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director create(Director director) {

        if (director.getId() != null && !director.getName().isBlank()) {

            String sqlQuery = "INSERT INTO directors (director_name) VALUES ( ? )";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
                stmt.setString(1, director.getName());
                return stmt;
            }, keyHolder);

            director.setId(keyHolder.getKey().intValue());

            return director;
        }
        return null;
    }

    @Override
    public boolean update(Director director) {

        if (!read(director.getId()).isEmpty()) {

            String sqlQuery = "UPDATE directors SET " +
                    "director_name = ? WHERE director_id = ?";

            return jdbcTemplate.update(sqlQuery
                    , director.getName()
                    , director.getId()) > 0;
        }

        return false;
    }

    @Override
    public List<Director> readAll() {
        return jdbcTemplate.query("SELECT * FROM directors", this::mapRowToDirector);

    }

    @Override
    public Optional<Director> read(int id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject("SELECT * FROM directors WHERE director_id = ?", this::mapRowToDirector, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(int id) {

        String sqlQuery = "DELETE FROM directors where director_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }



    @Override
    public void setFilmDirectors(Film film) {

        if(film.getDirectors() == null) {
            deleteFilmDirectors(film.getId());
            return;
        }

        deleteFilmDirectors(film.getId());

        String sqlQuery2 = "INSERT INTO film_directors (film_id, director_id) " +
                "VALUES (?, ?)";

        if (!film.getDirectors().isEmpty()) {

            for (Integer directorId : film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toList())) {
                jdbcTemplate.update(sqlQuery2
                        , film.getId()
                        , directorId);
            }
        }
    }

    @Override
    public List<Director> loadFilmDirectors(int filmId) {

        try {
            return jdbcTemplate.query("SELECT * FROM directors WHERE director_id IN (SELECT director_id " +
                    "FROM film_directors WHERE film_id=?)", this::mapRowToDirector, filmId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteFilmDirectors(int filmId) {
        String sqlQuery = "DELETE FROM film_directors WHERE film_id = ?";
        try {
            return jdbcTemplate.update(sqlQuery, filmId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public void updateFilmDirectors(Film film) {

        deleteFilmDirectors(film.getId());
        setFilmDirectors(film);
    }



    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("director_name"))
                .build();
    }
}