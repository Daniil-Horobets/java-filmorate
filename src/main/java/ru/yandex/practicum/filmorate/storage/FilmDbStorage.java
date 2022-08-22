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
        if (by.size() == 1 && by.get(0).equals("title")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID " +
                            "where locate(lower(?), lower(f.film_name)) GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
        }

        if (by.size() == 1 && by.get(0).equals("director")) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID " +
                            "WHERE locate(lower(?), lower(d.director_name)) GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, query);
        }

        if (by.containsAll((List.of("title", "director")))) {
            final String sqlQuery =
                    "SELECT * " +
                            "FROM films f " +
                            "JOIN mpa m ON f.film_mpa_id = m.mpa_id " +
                            "LEFT JOIN film_directors fd on f.film_id = fd.film_id " +
                            "LEFT JOIN directors d ON d.director_id = fd.director_id LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID " +
                            "WHERE locate(lower(?), lower(d.director_name)) or locate(lower(?), lower(f.film_name)) GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC";
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
        genreDbStorage.setFilmGenre(film);
        film.setGenres(genreDbStorage.loadFilmGenre(film.getId()));
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
    public void setMark(User user, Film film, int mark) {
        deleteMark(user.getId(), film.getId());
        final String sqlQuery = "INSERT INTO MARKS (film_id, user_id, mark) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), user.getId(), mark);
        setFilmRating(film.getId());
    }

    private void setFilmRating(int filmId) {

        deleteFilmRating(filmId);
        try {
            final String sqlQuery = "SELECT ROUND(AVG(m.MARK),1) FROM MARKS m WHERE m.FILM_ID=?";
            Optional<Double> filmRating = Optional.of(jdbcTemplate.queryForObject(sqlQuery, Double.class, filmId));
            if (!filmRating.isEmpty()) {
                final String sqlQuery2 = "INSERT INTO FILM_RATINGS (film_id, film_rating) VALUES (?, ?)";
                jdbcTemplate.update(sqlQuery2, filmId, filmRating.get());
            }
        } catch (NullPointerException e) {
            return;
        }
    }

    @Override
    public void updateFilmRatings(int userId) {
        try {
            final String sqlQuery = "SELECT f.FILM_ID FROM FILMS f JOIN MARKS m WHERE f.FILM_ID=m.FILM_ID AND USER_ID=?";
            List<Integer> filmIds = jdbcTemplate.queryForList(sqlQuery, Integer.class, userId);

            for (int filmId : filmIds) {
                deleteMark(userId, filmId);
            }
        } catch (EmptyResultDataAccessException e) {
            return;
        }
    }

    @Override
    public Optional<Double> readFilmRating(int filmId) {
        try {
            final String sqlQueryForLikes = "SELECT FILM_RATING FROM FILM_RATINGS WHERE film_id =?";
            return Optional.of(jdbcTemplate.queryForObject(sqlQueryForLikes, Double.class, filmId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public boolean deleteMark(int userId, int filmId) {
        try {
            final String sqlQuery = "DELETE FROM MARKS WHERE film_id = ? AND user_id = ?";
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
            final String sqlQuery = "DELETE FROM FILM_RATINGS WHERE film_id = ?";
            return jdbcTemplate.update(sqlQuery, filmId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public List<Film> readBestDirectorFilms(int directorId, String param) {

        if (directorRepository.read(directorId).isEmpty()) {
            return null;
        }

        String sqlQuery;

        if (param.equals("rate")) {
            sqlQuery = "SELECT f.film_id, f.film_name, f.film_description, f.film_release_date, f.film_duration, m.mpa_id, m.mpa_name FROM films f JOIN MPA m ON f.FILM_MPA_ID = m.MPA_ID JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID AND fd.DIRECTOR_ID=? LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC";
        } else if (param.equals("year")) {
            sqlQuery = "SELECT f.film_id, f.film_name, f.film_description, f.film_release_date, f.film_duration, m.mpa_id, m.mpa_name FROM films f JOIN MPA m ON f.FILM_MPA_ID = m.MPA_ID JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID AND fd.DIRECTOR_ID=? GROUP BY f.FILM_ID, f.FILM_RELEASE_DATE ORDER BY f.FILM_RELEASE_DATE DESC";
        } else {
            return null;
        }

        return jdbcTemplate.query(sqlQuery, this::mapToFilm, directorId);
    }

    @Override
    public List<Film> readBestFilms(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        String sqlQuery;
        if (genreId.isEmpty() && year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, count);
        } else if (!genreId.isEmpty() && year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID JOIN GENRES_OF_FILMS fg ON f.FILM_ID = fg.FILM_ID AND fg.GENRE_ID=? LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, genreId.get(), count);
        } else if (genreId.isEmpty() && !year.isEmpty()) {
            sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID WHERE YEAR(f.FILM_RELEASE_DATE)=? GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::mapToFilm, year.get(), count);
        }
        sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID JOIN GENRES_OF_FILMS fg ON f.FILM_ID = fg.FILM_ID AND fg.GENRE_ID=? LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID WHERE YEAR(f.FILM_RELEASE_DATE)=? GROUP BY f.FILM_ID, fr.FILM_RATING ORDER BY fr.FILM_RATING DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapToFilm, genreId.get(), year.get(), count);
    }

    @Override
    public List<Film> getRecommendations(Integer id) {
        final String sqlQuery = "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID WHERE f.FILM_ID IN" +
                "(SELECT film_id FROM MARKS " +
                "WHERE MARK > 5 AND user_id IN ( " +
                "SELECT l.user_id FROM MARKS l " +
                "WHERE l.film_id IN ( " +
                "SELECT film_id FROM MARKS " +
                "WHERE user_id = ? " +
                ") AND user_id != ?)) " +
                "GROUP BY f.FILM_ID, fr.FILM_RATING " +
                "ORDER BY FILM_RATING DESC";
        return jdbcTemplate.query(sqlQuery, this::mapToFilm, id, id);
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
        film.setGenres(genreDbStorage.loadFilmGenre(resultSet.getInt("film_id")));
        film.setDirectors(directorRepository.loadFilmDirectors(resultSet.getInt("film_id")));
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
                "SELECT * FROM films f JOIN MPA M on f.FILM_MPA_ID = M.MPA_ID LEFT JOIN FILM_RATINGS fr ON f.FILM_ID = fr.FILM_ID WHERE f.FILM_ID IN" +
                        "(select m1.FILM_ID from MARKS m1 JOIN " +
                        "MARKS m2 on m1.FILM_ID = m2.FILM_ID AND m1.USER_ID = ? AND m2.USER_ID =?) group by f.FILM_ID, fr.FILM_RATING order by fr.FILM_RATING desc";
        return jdbcTemplate.query(sqlQueryCommonFilms, this::mapToFilm, userId, friendId);
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
