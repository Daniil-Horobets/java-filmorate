package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Qualifier("userDbStorage")
    @Autowired
    private UserStorage userStorage;

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        checkUserExistence(id, userStorage);
        return userStorage.get(id);
    }

    public User create(User user) {
        UserValidator.validateUser(user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        checkUserExistence(user.getId(), userStorage);
        UserValidator.validateUser(user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        checkUserExistence(userId, userStorage);
        checkUserExistence(friendId, userStorage);
        userStorage.addFriend(userStorage.get(userId), userStorage.get(friendId));
    }

    public void deleteFriend(int userId, int friendId) {
        checkUserExistence(userId, userStorage);
        checkUserExistence(friendId, userStorage);
        userStorage.deleteFriend(userStorage.get(userId), userStorage.get(friendId));
    }

    public List<User> getFriends(int id) {
        checkUserExistence(id, userStorage);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        checkUserExistence(id, userStorage);
        checkUserExistence(otherId, userStorage);
        List<User> list = new ArrayList<>();
        List<User> users = getFriends(otherId);
        Set<User> uniqueValues = new HashSet<>();
        for (User user : getFriends(id)) {
            if (uniqueValues.add(user)) {
                if (users.contains(user)) {
                    list.add(user);
                }
            }
        }
        return list;
    }

    public void checkUserExistence(int userId, UserStorage userStorage) {
        User userToFind = new User();
        userToFind.setId(userId);
        if (!userStorage.getAll().contains(userToFind)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
    }

    public boolean delete(int id) {
        return userStorage.delete(id);
    }
}
