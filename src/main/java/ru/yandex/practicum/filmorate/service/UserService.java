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
    InMemoryUserStorage inMemoryUserStorage;

    public List<User> getAll() {
        return inMemoryUserStorage.getAll();
    }

    public User get(int id) {
        if (!inMemoryUserStorage.getUsers().containsKey(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }
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
        if (!inMemoryUserStorage.getUsers().containsKey(user.getId())) {
            throw new NotFoundException("User with id=" + user.getId() + " not found");
        }
        UserValidator.validateUser(user);
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return inMemoryUserStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        User user = inMemoryUserStorage.get(userId);
        User friend = inMemoryUserStorage.get(friendId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        if (friend == null) {
            throw new NotFoundException("User with id=" + friendId + " not found");
        }
        inMemoryUserStorage.addFriend(user, friend);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = inMemoryUserStorage.get(userId);
        User friend = inMemoryUserStorage.get(friendId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        if (friend == null) {
            throw new NotFoundException("User with id=" + friendId + " not found");
        }
        inMemoryUserStorage.deleteFriend(user, friend);
    }

    public List<User> getFriends(int id) {
        if (!inMemoryUserStorage.getUsers().containsKey(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }
        return inMemoryUserStorage.get(id).getFriendsIds()
                .stream()
                .map(inMemoryUserStorage::get)
                .collect((Collectors.toList()));
    }

    public List<User> getCommonFriends(int id, int otherId) {
        if (!inMemoryUserStorage.getUsers().containsKey(id)) {
            throw new NotFoundException("User with id=" + id + " not found");
        }
        if (!inMemoryUserStorage.getUsers().containsKey(otherId)) {
            throw new NotFoundException("User with id=" + otherId + " not found");
        }
        return getFriends(id)
                .stream()
                .distinct()
                .filter(getFriends(otherId)::contains)
                .collect((Collectors.toList()));
    }
}
