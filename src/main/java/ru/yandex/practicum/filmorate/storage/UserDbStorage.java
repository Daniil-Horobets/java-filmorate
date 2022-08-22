package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final FilmStorage filmStorage;

    public UserDbStorage(JdbcTemplate jdbcTemplate, FilmStorage filmStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = filmStorage;
    }

    @Override
    public List<User> getAll() {
        final String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::mapToUser);
    }

    @Override
    public User get(int id) {
        final String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sqlQuery, this::mapToUser, id);
        if (users.size() != 1) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User create(User user) {
        final String sqlQuery =
                "INSERT INTO users(user_email, user_login, user_name, user_birthday) " +
                "VALUES (?, ?, ?, ?)";


        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            final LocalDate birthday = user.getBirthday();
            if (birthday == null) {
                stmt.setNull(4, Types.DATE);
            } else {
                stmt.setDate(4, Date.valueOf(birthday));
            }
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        final String sqlQuery = "UPDATE users SET user_email = ?, user_login = ?, user_name = ?, user_birthday = ?" +
                " WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery
                , user.getEmail()
                , user.getLogin()
                , user.getName()
                , user.getBirthday()
                , user.getId());
        return user;
    }

    @Override
    public void addFriend(User user, User friend) {
        final String sqlQuery = "INSERT INTO friendship(user_id, user_friend_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setInt(1, user.getId());
            stmt.setInt(2, friend.getId());
            return stmt;
        }, keyHolder);
    }

    @Override
    public void deleteFriend(User user, User friend) {
        final String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND user_friend_id = ?";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setInt(1, user.getId());
            stmt.setInt(2, friend.getId());
            return stmt;
        }, keyHolder);
    }

    @Override
    public List<User> getFriends(int id) {
        final String sqlQuery =
                "SELECT * " +
                "FROM users u " +
                "WHERE u.user_id " +
                "IN (SELECT f.user_friend_id " +
                    "FROM friendship f " +
                    "WHERE f.user_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::mapToUser, id);
    }

    @Override
    public boolean delete(int id) {
        try {
        String sqlQuery = "DELETE FROM USERS where USER_ID = ?";
        filmStorage.updateFilmRatings(id);
        return jdbcTemplate.update(sqlQuery, id) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private User mapToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setEmail(resultSet.getString("user_email"));
        user.setLogin(resultSet.getString("user_login"));
        user.setName(resultSet.getString("user_name"));
        user.setBirthday(resultSet.getDate("user_birthday").toLocalDate());
        return user;
    }
}
