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

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, DirectorStorage directorStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
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
        if (by.size() == 1 && by.get(0).equals("title")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id LEFT JOIN film_ratings fr ON f.film_id = fr.film_id " +
                            "where locate(lower(?), lower(f.film_name)) GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
        }

        if (by.size() == 1 && by.get(0).equals("director")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id LEFT JOIN film_ratings fr ON f.film_id = fr.film_id " +
                            "WHERE locate(lower(?), lower(d.director_name)) GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
        }

        if (by.containsAll((List.of("title", "director")))) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id LEFT JOIN film_ratings fr ON f.film_id = fr.film_id " +
                            "WHERE locate(lower(?), lower(d.director_name)) or locate(lower(?), lower(f.film_name)) GROUP BY f.film_id, fr.film_rating ORDER BY f.film_id DESC";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query, query);
        }
        return null;
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
        genreStorage.setFilmGenre(film);
        film.setGenres(genreStorage.loadFilmGenre(film.getId()));
        directorStorage.setFilmDirectors(film);
        film.setDirectors(directorStorage.loadFilmDirectors(film.getId()));

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
        genreStorage.setFilmGenre(film);
        film.setGenres(genreStorage.loadFilmGenre(film.getId()));
        directorStorage.setFilmDirectors(film);
        film.setDirectors(directorStorage.loadFilmDirectors(film.getId()));

        return film;
    }

    @Override
    public void setMark(User user, Film film, int mark) {
        deleteMark(user.getId(), film.getId());
        final String sqlQuery = "INSERT INTO marks (film_id, user_id, mark) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId(), mark);
        setFilmRating(film.getId());
    }

    private void setFilmRating(int filmId) {

        deleteFilmRating(filmId);
        try {
            final String sqlQuery = "SELECT ROUND(AVG(m.MARK),1) FROM marks m WHERE m.film_id=?";
            Optional<Double> filmRating = Optional.of(jdbcTemplate.queryForObject(sqlQuery, Double.class, filmId));
            if (!filmRating.isEmpty()) {
                final String sqlQuery2 = "INSERT INTO film_ratings (film_id, film_rating) VALUES (?, ?)";
                jdbcTemplate.update(sqlQuery2, filmId, filmRating.get());
            }
        } catch (NullPointerException e) {
        }
    }

    @Override
    public void updateFilmRatings(int userId) {
        try {
            final String sqlQuery = "SELECT f.film_id FROM FILMS f JOIN marks m WHERE f.film_id=m.film_id AND USER_ID=?";
            List<Integer> filmIds = jdbcTemplate.queryForList(sqlQuery, Integer.class, userId);

            for (int filmId : filmIds) {
                deleteMark(userId, filmId);
            }
        } catch (EmptyResultDataAccessException e) {
        }
    }

    @Override
    public Optional<Double> readFilmRating(int filmId) {
        try {
            final String sqlQueryForLikes = "SELECT film_rating FROM film_ratings WHERE film_id =?";
            return Optional.of(jdbcTemplate.queryForObject(sqlQueryForLikes, Double.class, filmId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public boolean deleteMark(int userId, int filmId) {
        try {
            final String sqlQuery = "DELETE FROM marks WHERE film_id = ? AND user_id = ?";
            if (jdbcTemplate.update(sqlQuery, filmId, userId) > 0) {
                setFilmRating(filmId);
                return true;
            }
            return false;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }



    private boolean deleteFilmRating(int filmId) {
        try {
            final String sqlQuery = "DELETE FROM film_ratings WHERE film_id = ?";
            return jdbcTemplate.update(sqlQuery, filmId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public List<Film> readBestDirectorFilms(int directorId, String param) {

        if (directorStorage.read(directorId).isEmpty()) {
            return null;
        }

        String sqlQuery;

        if (param.equals("likes")) {
            sqlQuery = "SELECT * FROM films f JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                    "JOIN FILM_DIRECTORS fd ON f.film_id = fd.film_id AND fd.DIRECTOR_ID=? " +
                    "LEFT JOIN film_ratings fr ON f.film_id = fr.film_id " +
                    "GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC";
        } else if (param.equals("year")) {
            sqlQuery = "SELECT * FROM films f JOIN MPA m ON f.film_mpa_id = m.mpa_id " +
                    "JOIN FILM_DIRECTORS fd ON f.film_id = fd.film_id AND fd.DIRECTOR_ID=? " +
                    "GROUP BY f.film_id, f.FILM_RELEASE_DATE ORDER BY f.FILM_RELEASE_DATE";
        } else {
            return null;
        }

        return jdbcTemplate.query(sqlQuery, this::mapToFilm, directorId);
    }

    @Override
    public List<Film> readBestFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        String sqlQuery;
        if (genreId.isEmpty() && year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.film_mpa_id = M.mpa_id " +
                    "LEFT JOIN film_ratings fr ON f.film_id = fr.film_id " +
                    "GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, count);
        } else if (!genreId.isEmpty() && year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.film_mpa_id = M.mpa_id " +
                    "JOIN GENRES_OF_FILMS fg ON f.film_id = fg.film_id AND fg.GENRE_ID=? " +
                    "LEFT JOIN film_ratings fr ON f.film_id = fr.film_id " +
                    "GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, genreId.get(), count);
        } else if (genreId.isEmpty() && !year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.film_mpa_id = M.mpa_id " +
                    "LEFT JOIN film_ratings fr ON f.film_id = fr.film_id WHERE YEAR(f.FILM_RELEASE_DATE)=? " +
                    "GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, year.get(), count);
        }
        sqlQuery = "SELECT * FROM films f JOIN MPA M on f.film_mpa_id = M.mpa_id " +
                "JOIN GENRES_OF_FILMS fg ON f.film_id = fg.film_id AND fg.GENRE_ID=? " +
                "LEFT JOIN film_ratings fr ON f.film_id = fr.film_id WHERE YEAR(f.FILM_RELEASE_DATE)=? " +
                "GROUP BY f.film_id, fr.film_rating ORDER BY fr.film_rating DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapToFilm, genreId.get(), year.get(), count);
    }

    @Override
    public List<Film> getRecommendations(Integer id) {

        final String sqlQuery = "SELECT f.film_id, f.film_release_date, f.film_duration, f.film_description, f.film_name, m.mpa_id, m.mpa_name FROM films f JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                "JOIN marks fm ON f.film_id = fm.film_id AND fm.mark > 5 WHERE f.film_id IN " +
                "(SELECT m1.film_id FROM marks m1, marks m2, marks m3 " +
                "WHERE m2.user_id = ? AND m3.film_id = m2.film_id AND m3.user_id != m2.user_id " +
                "AND m1.film_id != m2.film_id AND m1.user_id = m3.user_id)" +
                "GROUP BY f.film_id ORDER BY f.film_id DESC ";

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
        film.setUserRating(loadFilmRating(resultSet.getInt("film_id")));
        film.setGenres(genreStorage.loadFilmGenre(resultSet.getInt("film_id")));
        film.setDirectors(directorStorage.loadFilmDirectors(resultSet.getInt("film_id")));
        return film;
    }

    private String loadFilmRating(int filmId) {
        if (readFilmRating(filmId).isEmpty()) {
            return "Not rated yet";
        } else {
            return String.valueOf(readFilmRating(filmId).get());
        }
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        final String sqlQueryCommonFilms =
                "SELECT * FROM films f JOIN mpa m ON F.film_mpa_id = M.mpa_id LEFT JOIN film_ratings fr ON f.film_id = fr.film_id WHERE f.film_id IN" +
                        "(select m1.film_id FROM marks m1 JOIN " +
                        "marks m2 on m1.film_id = m2.film_id AND m1.user_id = ? AND m2.user_id =?) GROUP BY f.film_id, fr.film_rating order by fr.film_rating desc";
        return jdbcTemplate.query(sqlQueryCommonFilms, this::mapToFilm, userId, friendId);
    }

    @Override
    public boolean delete(int filmId) {
        String sqlQuery = "DELETE FROM FILMS where film_id = ?";

        try {
            return jdbcTemplate.update(sqlQuery, filmId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
