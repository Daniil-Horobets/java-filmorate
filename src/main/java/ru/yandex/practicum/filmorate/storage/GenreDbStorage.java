package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Genre mapToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("GENRE_ID"));
        genre.setName(resultSet.getString("GENRE_NAME"));
        return genre;
    }

    public List<Genre> getAll() {
        final String sqlQuery = "SELECT * FROM GENRES ORDER BY GENRE_ID";
        return jdbcTemplate.query(sqlQuery, this::mapToGenre);
    }

    public Genre get(int id) {
        final String sqlQuery = "SELECT * FROM GENRES WHERE GENRE_ID = ?";
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
        final String sqlQueryDelete = "DELETE FROM GENRES_OF_FILMS WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQueryDelete, film.getId());

        for (Genre genre : film.getGenres()) {
            final String sqlQueryInsert = "INSERT INTO GENRES_OF_FILMS (GENRE_ID, FILM_ID) VALUES (?, ?)";
            jdbcTemplate.update(sqlQueryInsert, genre.getId(), film.getId());
        }
    }

    @Override
    public void loadFilmGenre(Film film) {
        final String sqlQuery =
                "SELECT G.GENRE_ID, G.GENRE_NAME " +
                "FROM GENRES_OF_FILMS GOF " +
                "JOIN GENRES G ON GOF.GENRE_ID = G.GENRE_ID " +
                "WHERE GOF.FILM_ID =?";
        Set<Genre> genres = new HashSet<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQuery, film.getId());

        for (Map row : rows) {
            Genre genre = new Genre();

            genre.setId((Integer) row.get("GENRE_ID"));
            genre.setName((String) row.get("GENRE_NAME"));
            genres.add(genre);
        }
        film.setGenres(genres);
    }

}
