package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter = 0;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User get(int id) {
        return users.get(id);
    }

    @Override
    public User create(User user) {
        user.setId(++idCounter);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void addFriend(User user, User friend) {
        user.getFriendsIds().add(friend.getId());
        friend.getFriendsIds().add(user.getId());
    }

    @Override
    public void deleteFriend(User user, User friend) {
        user.getFriendsIds().remove(friend.getId());
        friend.getFriendsIds().remove(user.getId());
    }
}
