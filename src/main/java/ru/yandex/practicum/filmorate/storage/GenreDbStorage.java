package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
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
    public Set<Genre> loadFilmGenre(int filmId) {

        Set<Genre> filmGenres = new HashSet<>();

        try {
            for(Genre genre : jdbcTemplate.query("SELECT * FROM GENRES WHERE GENRE_ID IN (SELECT GENRE_ID " +
                    "FROM GENRES_OF_FILMS WHERE FILM_ID=?)", this::mapToGenre, filmId)) {
                filmGenres.add(genre);
            }

            return filmGenres;

        } catch (EmptyResultDataAccessException e) {
            return new HashSet<>();
        }
    }

    private Genre mapToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("genre_id"));
        genre.setName(resultSet.getString("genre_name"));
        return genre;
    }

}
