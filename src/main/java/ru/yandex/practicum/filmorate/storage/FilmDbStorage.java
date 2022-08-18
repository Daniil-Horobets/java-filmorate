package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDbStorage genreDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public List<Film> getAll() {
        final String sqlQuery =
                "SELECT f.film_id, " +
                "f.film_name, " +
                "f.film_description, " +
                "f.film_release_date, " +
                "f.film_duration, " +
                "m.mpa_id, " +
                "m.mpa_name " +
                "FROM films f " +
                "JOIN mpa m " +
                "ON f.film_mpa_id = m.mpa_id";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapToFilm);
        for (Film film : films) {
            genreDbStorage.loadFilmGenre(film);
            loadFilmLikes(film);
        }
        return films;
    }

    @Override
    public Film get(int id) {
        final String sqlQuery =
                "SELECT f.film_id, " +
                "f.film_name, " +
                "f.film_description, " +
                "f.film_release_date, " +
                "f.film_duration, " +
                "m.mpa_id, " +
                "m.mpa_name " +
                "FROM films f " +
                "JOIN mpa m " +
                "ON f.film_mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapToFilm, id);
        if (films.size() != 1) {
            return null;
        }
        Film film = films.get(0);
        genreDbStorage.loadFilmGenre(film);
        loadFilmLikes(film);
        return film;
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery =
                "INSERT INTO films(film_name, film_description, film_release_date, film_duration, film_mpa_id) " +
                "VALUES (?, ?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            final LocalDate releaseDate = film.getReleaseDate();
            if (releaseDate == null) {
                stmt.setNull(3, Types.DATE);
            } else {
                stmt.setDate(3, Date.valueOf(releaseDate));
            }
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        genreDbStorage.setFilmGenre(film);
        genreDbStorage.loadFilmGenre(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        final String sqlQuery = "UPDATE films SET film_name = ?, film_description = ?, film_release_date = ?, " +
                "film_duration = ?, film_mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery
                , film.getName()
                , film.getDescription()
                , film.getReleaseDate()
                , film.getDuration()
                , film.getMpa().getId()
                , film.getId());
        genreDbStorage.setFilmGenre(film);
        genreDbStorage.loadFilmGenre(film);
        return film;
    }

    @Override
    public void addLike(User user, Film film) {
        final String sqlQuery = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
        film.getLikedUsersIds().add(user.getId());
    }

    @Override
    public void deleteLike(User user, Film film) {
        final String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
    }

    private Film mapToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("film_id"));
        film.setName(resultSet.getString("film_name"));
        film.setDescription(resultSet.getString("film_description"));
        film.setReleaseDate(resultSet.getDate("film_release_date").toLocalDate());
        film.setDuration(resultSet.getInt("film_duration"));
        film.setMpa(new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("mpa_name")));
        return film;
    }

    private void loadFilmLikes(Film film) {
        final String sqlQueryForLikes =
                "SELECT user_id " +
                        "FROM likes " +
                        "WHERE film_id =?";
        Set<Integer> likedUsersIds = new HashSet<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQueryForLikes, film.getId());
        for (Map<String, Object> row : rows) {
            likedUsersIds.add((Integer) row.get("user_id"));
        }
        film.setLikedUsersIds(likedUsersIds);
    }

    public List<Film> getCommonFilms(int userId, int friendId){
        final String sqlQueryCommonFilms =
                "SELECT film_id FROM likes WHERE (user_id = ? OR user_id =?) " +
                        "GROUP BY film_id HAVING COUNT(film_id)=2";
        List<Integer> ids = jdbcTemplate.queryForList(sqlQueryCommonFilms,Integer.class,userId,friendId);
        List<Film> films = new ArrayList<>(Collections.emptyList());
        for (int filmId : ids) {
            films.add(get(filmId));
        }
        return films;
    }

    @Override
    public boolean delete(int filmId) {
        String sqlQuery = "DELETE FROM FILMS where FILM_ID = ?";

        try {
            return jdbcTemplate.update(sqlQuery, filmId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
