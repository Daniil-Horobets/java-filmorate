package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAll();
    User get(int id);
    User create(User user);
    User update(User user);
    void addFriend(User user, User friend);
    void deleteFriend(User user, User friend);
    List<User> getFriends(int id);
    boolean delete(int id);
}