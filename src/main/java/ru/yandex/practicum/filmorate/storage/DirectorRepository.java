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
public class DirectorRepository implements IDirectorRepository{

    private final JdbcTemplate jdbcTemplate;

    public DirectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director create(Director director) {

        if (director.getId() != null && !director.getName().isBlank()) {

            String sqlQuery = "insert into DIRECTORS (DIRECTOR_NAME) values ( ? )";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"DIRECTOR_ID"});
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

            String sqlQuery = "update DIRECTORS set " +
                    "DIRECTOR_NAME = ? where DIRECTOR_ID = ?";

            return jdbcTemplate.update(sqlQuery
                    , director.getName()
                    , director.getId()) > 0;
        }

        return false;
    }

    @Override
    public List<Director> readAll() {
        return jdbcTemplate.query("SELECT * FROM DIRECTORS", this::mapRowToDirector);

    }

    @Override
    public Optional<Director> read(int id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject("SELECT * FROM DIRECTORS WHERE DIRECTOR_ID = ?", this::mapRowToDirector, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(int id) {

        String sqlQuery = "DELETE FROM DIRECTORS where DIRECTOR_ID = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }



    @Override
    public void setFilmDirectors(Film film) {

        if(film.getDirectors() == null) {
            deleteFilmDirectors(film.getId());
            return;
        }

        deleteFilmDirectors(film.getId());

        String sqlQuery2 = "INSERT into FILM_DIRECTORS (FILM_ID, DIRECTOR_ID) " +
                "values (?, ?)";

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
            return jdbcTemplate.query("SELECT * FROM DIRECTORS WHERE DIRECTOR_ID IN (SELECT DIRECTOR_ID " +
                    "FROM FILM_DIRECTORS WHERE FILM_ID=?)", this::mapRowToDirector, filmId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteFilmDirectors(int filmId) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS where FILM_ID = ?";
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
                .id(resultSet.getInt("DIRECTOR_ID"))
                .name(resultSet.getString("DIRECTOR_NAME"))
                .build();
    }
}