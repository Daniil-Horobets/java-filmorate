package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        final String sqlQueryInsert = "INSERT INTO genres_of_films (genre_id, film_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sqlQueryInsert, genre.getId(), film.getId());
        }
    }

    @Override
    public void loadFilmGenre(Film film) {
        final String sqlQuery =
                "SELECT g.genre_id, g.genre_name " +
                "FROM genres_of_films gof " +
                "JOIN genres g ON gof.genre_id = g.genre_id " +
                "WHERE gof.film_id =?";

        Set<Genre> set = new HashSet<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(sqlQuery, film.getId())) {
            Genre genre = new Genre(
                    (Integer) row.get("genre_id"),
                    (String) row.get("genre_name"));
            set.add(genre);
        }
        film.setGenres(
                set
        );
    }

    private Genre mapToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("genre_id"));
        genre.setName(resultSet.getString("genre_name"));
        return genre;
    }

}
