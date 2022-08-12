package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private User mapToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("USER_ID"));
        user.setEmail(resultSet.getString("USER_EMAIL"));
        user.setLogin(resultSet.getString("USER_LOGIN"));
        user.setName(resultSet.getString("USER_NAME"));
        user.setBirthday(resultSet.getDate("USER_BIRTHDAY").toLocalDate());
        return user;
    }

    @Override
    public List<User> getAll() {
        final String sqlQuery = "SELECT * FROM USERS";
        return jdbcTemplate.query(sqlQuery, this::mapToUser);
    }

    @Override
    public User get(int id) {
        final String sqlQuery = "SELECT * FROM USERS WHERE USER_ID = ?";
        List<User> users = jdbcTemplate.query(sqlQuery, this::mapToUser, id);
        if (users.size() != 1) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public User create(User user) {
        final String sqlQuery =
                "INSERT INTO USERS(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY) " +
                "VALUES (?, ?, ?, ?)";


        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"USER_ID"});
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
        final String sqlQuery = "UPDATE USERS SET USER_EMAIL = ?, USER_LOGIN = ?, USER_NAME = ?, USER_BIRTHDAY = ?" +
                " WHERE USER_ID = ?";
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
        final String sqlQuery = "INSERT INTO FRIENDSHIP(USER_ID, USER_FRIEND_ID) VALUES (?, ?)";
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
        final String sqlQuery = "DELETE FROM FRIENDSHIP WHERE USER_ID = ? AND USER_FRIEND_ID = ?";
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
                "FROM USERS U " +
                "WHERE U.USER_ID " +
                        "IN (SELECT F.USER_FRIEND_ID " +
                            "FROM FRIENDSHIP F " +
                            "WHERE F.USER_ID = ?)";
        return jdbcTemplate.query(sqlQuery, this::mapToUser, id);

    }
}
