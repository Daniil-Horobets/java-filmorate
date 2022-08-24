package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;
    private final DirectorRepository directorRepository;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDbStorage genreDbStorage, DirectorRepository directorRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.directorRepository = directorRepository;
    }

    @Override
    public List<Film> getAll() {
        final String sqlQuery =
                "SELECT * FROM films f " +
                        "JOIN mpa m " +
                        "ON f.film_mpa_id = m.mpa_id";
        return jdbcTemplate.query(sqlQuery, this::mapToFilm);
    }

    public List<Film> getFilmsByQuery(String query, List<String> by) {

        if (by == null || by.isEmpty()) {
            return null;
        }

        if (by.size() == 1 && by.get(0).equals("title")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "where locate(lower(?), lower(f.film_name))";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
        }
        if (by.size() == 1 && by.get(0).equals("director")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id " +
                            "WHERE locate(lower(?), lower(d.director_name))";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
        }

        final String sqlQuery =
                "SELECT * " +
                        "FROM films f " +
                        "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                        "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                        "LEFT JOIN directors d ON d.director_id = fd.director_id " +
                        "WHERE locate(lower(?), lower(d.director_name)) or locate(lower(?), lower(f.film_name))" +
                        "ORDER BY f.film_id DESC";

        return jdbcTemplate.query(sqlQuery, this::mapToFilm, query, query);
    }

    @Override
    public Film get(int id) {
        final String sqlQuery =
                "SELECT * FROM films f " +
                        "JOIN mpa m " +
                        "ON f.film_mpa_id = m.mpa_id " +
                        "WHERE f.film_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapToFilm, id);
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
        genreDbStorage.loadFilmGenre(film.getId());
        directorRepository.setFilmDirectors(film);
        film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));

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
        film.setGenres(genreDbStorage.loadFilmGenre(film.getId()));
        directorRepository.setFilmDirectors(film);
        film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));

        return film;
    }

    @Override
    public void addLike(User user, Film film) {
        final String sqlQuery = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
    }

    @Override
    public void deleteLike(User user, Film film) {
        final String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId());
    }

    @Override
    public List<Film> readBestDirectorFilms(int directorId, String param) {

        if (directorRepository.read(directorId).isEmpty()) {
            return null;
        }

        String sqlQuery;

        if (param.equals("likes")) {
            sqlQuery = "SELECT * FROM films f JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                    "RIGHT JOIN film_directors fd ON f.film_id = fd.film_id AND fd.director_id=? " +
                    "LEFT JOIN likes fl ON f.film_id = fl.film_id " +
                    "GROUP BY f.FILM_ID, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC";
        } else if (param.equals("year")) {
            sqlQuery = "SELECT * FROM films f JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id AND fd.director_id=? " +
                    "GROUP BY f.film_id ORDER BY f.FILM_RELEASE_DATE";
        } else {
            return null;
        }

        return jdbcTemplate.query(sqlQuery, this::mapToFilm, directorId);
    }

    @Override
    public List<Film> getRecommendations(Integer id) {
        final String sqlQuery = "SELECT * FROM films f JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                "LEFT JOIN likes fl ON f.film_id = fl.film_id WHERE f.film_id IN " +
                "(SELECT l1.film_id FROM likes l1, likes l2, likes l3 " +
                "WHERE l2.user_id = ? AND l3.film_id = l2.film_id AND l3.user_id != l2.user_id " +
                "AND l1.film_id != l2.film_id AND l1.user_id = l3.user_id)" +
                "GROUP BY f.film_id, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC";

        return jdbcTemplate.query(sqlQuery, this::mapToFilm, id);
    }

    private Film mapToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("film_id"));
        film.setName(resultSet.getString("film_name"));
        film.setDescription(resultSet.getString("film_description"));
        film.setReleaseDate(resultSet.getDate("film_release_date").toLocalDate());
        film.setDuration(resultSet.getInt("film_duration"));
        film.setMpa(new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("mpa_name")));
        film.setDirectors(directorRepository.loadFilmDirectors(resultSet.getInt("film_id")));
        film.setGenres(genreDbStorage.loadFilmGenre(resultSet.getInt("film_id")));
        return film;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        final String sqlQueryCommonFilms =
                "SELECT * FROM films f JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                        "LEFT JOIN likes fl ON f.film_id = fl.film_id WHERE f.FILM_ID IN" +
                        " (SELECT l.film_id FROM likes l, likes l1, likes l2 " +
                        "WHERE l1.user_id = ? AND l2.user_id = ? " +
                        "AND l.film_id=l1.film_id AND l.film_id=l2.film_id) " +
                        "GROUP BY f.film_id, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC";

        return jdbcTemplate.query(sqlQueryCommonFilms, this::mapToFilm, userId, friendId);
    }

    @Override
    public boolean delete(int filmId) {
        String sqlQuery = "DELETE FROM films where film_id = ?";

        try {
            return jdbcTemplate.update(sqlQuery, filmId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public List<Film> getMostLikedFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        String sqlQuery;
        if (genreId.isEmpty() && year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID LEFT JOIN likes fl ON f.film_id = fl.film_id GROUP BY f.FILM_ID, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, count);
        } else if (!genreId.isEmpty() && year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID JOIN GENRES_OF_FILMS fg ON f.FILM_ID = fg.FILM_ID AND fg.GENRE_ID=? LEFT JOIN likes fl ON f.film_id = fl.film_id GROUP BY f.FILM_ID, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, genreId.get(), count);
        } else if (genreId.isEmpty() && !year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID LEFT JOIN likes fl ON f.film_id = fl.film_id WHERE YEAR(f.FILM_RELEASE_DATE)=? GROUP BY f.FILM_ID, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, year.get(), count);
        }
        sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID JOIN GENRES_OF_FILMS fg ON f.FILM_ID = fg.FILM_ID AND fg.GENRE_ID=? LEFT JOIN likes fl ON f.film_id = fl.film_id WHERE YEAR(f.FILM_RELEASE_DATE)=? GROUP BY f.FILM_ID, fl.film_id, fl.user_id ORDER BY COUNT(fl.FILM_ID) DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapToFilm, genreId.get(), year.get(), count);

    }

}
