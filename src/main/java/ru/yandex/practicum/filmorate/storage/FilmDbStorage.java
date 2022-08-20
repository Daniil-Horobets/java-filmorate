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
    private final DirectorRepository directorRepository;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDbStorage genreDbStorage, DirectorRepository directorRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.directorRepository = directorRepository;
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
            film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));
        }
        return films;
    }

    public List<Film> getFilmsByQuery (String query, List<String> by) {
        if (by.size() == 1 && by.get(0).equals("title")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "where locate(lower(?), lower(f.film_name))";
            List<Film> films = jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
            for (Film film : films) {
                genreDbStorage.loadFilmGenre(film);
                loadFilmLikes(film);
                film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));
            }
            return films;
        }
        if (by.size() == 1 && by.get(0).equals("director")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id " +
                            "WHERE locate(lower(?), lower(d.director_name))";
            List<Film> films = jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
            for (Film film : films) {
                genreDbStorage.loadFilmGenre(film);
                loadFilmLikes(film);
                film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));
            }
            return films;
        }
        if (by.containsAll((List.of("title", "director")))) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id " +
                            "WHERE locate(lower(?), lower(d.director_name)) or locate(lower(?), lower(f.film_name))";
            List<Film> films = jdbcTemplate.query(sqlQuery, this::mapToFilm, query, query);
            for (Film film : films) {
                genreDbStorage.loadFilmGenre(film);
                loadFilmLikes(film);
                film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));
            }
            return films;
        }
        return null;
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
        film.setDirectors(directorRepository.loadFilmDirectors(id));
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
        genreDbStorage.loadFilmGenre(film);
        directorRepository.setFilmDirectors(film);
        film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));

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
    @Override
    public List<Film> readBestDirectorFilms(int directorId, String param) {

        if(directorRepository.read(directorId).isEmpty()) {
            return null;
        }

        String sqlQuery;

        if (param.equals("likes")) {
            sqlQuery ="SELECT f.film_id, f.film_name, f.film_description, f.film_release_date, f.film_duration, m.mpa_id, m.mpa_name FROM films f JOIN MPA m ON f.FILM_MPA_ID = m.MPA_ID RIGHT JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID AND fd.DIRECTOR_ID=? LEFT JOIN LIKES fl ON f.FILM_ID = fl.FILM_ID GROUP BY f.FILM_ID ORDER BY COUNT(fl.FILM_ID) DESC";
        } else if (param.equals("year")) {
            sqlQuery ="SELECT f.film_id, f.film_name, f.film_description, f.film_release_date, f.film_duration, m.mpa_id, m.mpa_name FROM films f JOIN MPA m ON f.FILM_MPA_ID = m.MPA_ID RIGHT JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID AND fd.DIRECTOR_ID=? GROUP BY YEAR (f.FILM_RELEASE_DATE), MONTH(f.FILM_RELEASE_DATE), DAY_OF_MONTH(f.FILM_RELEASE_DATE)";
        } else {
            return null;
        }

        List<Film> directorFilms = jdbcTemplate.query(sqlQuery, this::mapToFilm, directorId);

        for (Film film : directorFilms) {
            genreDbStorage.loadFilmGenre(film);
            loadFilmLikes(film);
            film.setDirectors(directorRepository.loadFilmDirectors(film.getId()));
        }

        return directorFilms;
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
