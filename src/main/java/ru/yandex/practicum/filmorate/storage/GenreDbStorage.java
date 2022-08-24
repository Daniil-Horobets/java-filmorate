package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAll() {
        final String sqlQuery = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sqlQuery, this::mapToGenre);
    }

    @Override
    public Genre get(int id) {
        final String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sqlQuery, this::mapToGenre, id);
        if (genres.size() != 1) {
            return null;
        }
        return genres.get(0);
    }

    @Override
    public void setFilmGenre(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        final String sqlQueryDelete = "DELETE FROM genres_of_films WHERE film_id = ?";
        jdbcTemplate.update(sqlQueryDelete, film.getId());

        final String sqlQueryInsert = "MERGE INTO genres_of_films (genre_id, film_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sqlQueryInsert, genre.getId(), film.getId());
        }
    }

    @Override
    public List<Genre> loadFilmGenre(int filmId) {
        final String sqlQuery = "SELECT g.genre_id, g.genre_name " +
                "FROM genres_of_films gof " +
                "JOIN genres g ON gof.genre_id = g.genre_id " +
                "WHERE gof.film_id =?";

        return jdbcTemplate.query(sqlQuery, this::mapToGenre, filmId);
    }

    private Genre mapToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("genre_id"));
        genre.setName(resultSet.getString("genre_name"));
        return genre;
    }

}
