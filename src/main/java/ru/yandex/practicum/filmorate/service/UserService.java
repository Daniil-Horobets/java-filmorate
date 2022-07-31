package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private InMemoryUserStorage inMemoryUserStorage;

    public List<User> getAll() {
        return inMemoryUserStorage.getAll();
    }

    public User getById(int id) {
        checkUserExistence(id, inMemoryUserStorage);
        return inMemoryUserStorage.get(id);
    }

    public User create(User user) {
        UserValidator.validateUser(user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return inMemoryUserStorage.create(user);
    }

    public User update(User user) {
        checkUserExistence(user.getId(), inMemoryUserStorage);
        UserValidator.validateUser(user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return inMemoryUserStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        checkUserExistence(userId, inMemoryUserStorage);
        checkUserExistence(friendId, inMemoryUserStorage);
        inMemoryUserStorage.addFriend(inMemoryUserStorage.get(userId), inMemoryUserStorage.get(friendId));
    }

    public void deleteFriend(int userId, int friendId) {
        checkUserExistence(userId, inMemoryUserStorage);
        checkUserExistence(friendId, inMemoryUserStorage);
        inMemoryUserStorage.deleteFriend(inMemoryUserStorage.get(userId), inMemoryUserStorage.get(friendId));
    }

    public List<User> getFriends(int id) {
        checkUserExistence(id, inMemoryUserStorage);
        return inMemoryUserStorage.get(id).getFriendsIds()
                .stream()
                .map(inMemoryUserStorage::get)
                .collect((Collectors.toList()));
    }

    public List<User> getCommonFriends(int id, int otherId) {
        checkUserExistence(id, inMemoryUserStorage);
        checkUserExistence(otherId, inMemoryUserStorage);
        return getFriends(id)
                .stream()
                .distinct()
                .filter(getFriends(otherId)::contains)
                .collect((Collectors.toList()));
    }

    public void checkUserExistence(int userId, InMemoryUserStorage inMemoryUserStorage) {
        if (!inMemoryUserStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
    }
}
