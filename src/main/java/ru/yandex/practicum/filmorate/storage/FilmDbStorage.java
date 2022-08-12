package ru.yandex.practicum.filmorate.storage;

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

    private Film mapToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("FILM_ID"));
        film.setName(resultSet.getString("FILM_NAME"));
        film.setDescription(resultSet.getString("FILM_DESCRIPTION"));
        film.setReleaseDate(resultSet.getDate("FILM_RELEASE_DATE").toLocalDate());
        film.setDuration(resultSet.getInt("FILM_DURATION"));
        film.setMpa(new Mpa(resultSet.getInt("MPA_ID"), resultSet.getString("MPA_NAME")));
        return film;
    }

    @Override
    public List<Film> getAll() {
        final String sqlQuery =
                "SELECT F.FILM_ID, " +
                "F.FILM_NAME, " +
                "F.FILM_DESCRIPTION, " +
                "F.FILM_RELEASE_DATE, " +
                "F.FILM_DURATION, " +
                "M.MPA_ID, " +
                "M.MPA_NAME " +
                "FROM FILMS F " +
                "JOIN MPA M " +
                "ON F.FILM_MPA_ID = M.MPA_ID";
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
                "SELECT F.FILM_ID, " +
                "F.FILM_NAME, " +
                "F.FILM_DESCRIPTION, " +
                "F.FILM_RELEASE_DATE, " +
                "F.FILM_DURATION, " +
                "M.MPA_ID, " +
                "M.MPA_NAME " +
                "FROM FILMS F " +
                "JOIN MPA M " +
                "ON F.FILM_MPA_ID = M.MPA_ID " +
                "WHERE F.FILM_ID = ?";
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
                "INSERT INTO FILMS(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION, FILM_MPA_ID) " +
                "VALUES (?, ?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"FILM_ID"});
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
        final String sqlQuery = "UPDATE FILMS SET FILM_NAME = ?, FILM_DESCRIPTION = ?, FILM_RELEASE_DATE = ?, " +
                "FILM_DURATION = ?, FILM_MPA_ID = ? WHERE FILM_ID = ?";
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
        final String sqlQuery = "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
        film.getLikedUsersIds().add(user.getId());
    }

    @Override
    public void deleteLike(User user, Film film) {
        final String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
    }

    private void loadFilmLikes(Film film) {
        final String sqlQueryForLikes =
                "SELECT USER_ID " +
                        "FROM LIKES " +
                        "WHERE FILM_ID =?";
        Set<Integer> likedUsersIds = new HashSet<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQueryForLikes, film.getId());
        for (Map<String, Object> row : rows) {
            likedUsersIds.add((Integer) row.get("USER_ID"));
        }
        film.setLikedUsersIds(likedUsersIds);
    }
}
