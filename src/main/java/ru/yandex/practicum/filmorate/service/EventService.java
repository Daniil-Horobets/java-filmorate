package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.eventEnums.EventType;
import ru.yandex.practicum.filmorate.model.eventEnums.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class EventService {
    //TODO добавить методы к функциональности review

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    @Autowired
    private EventStorage eventStorage;

    @Autowired
    private UserStorage userStorage;

    public List<Event> getUserEvents(int userId) {
        if (userStorage.get(userId) == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        return eventStorage.getUserEvents(userId);
    }

    public void removeLikeEvent(int userId, int filmId) {
        createEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public void removeReviewEvent(int userId, int reviewId) {
        createEvent(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public void removeFriendEvent(int userId, int friendId) {
        createEvent(userId, EventType.REVIEW, Operation.REMOVE, friendId);
    }

    public void addLikeEvent(int userId, int filmId) {
        createEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void addReviewEvent(int userId, int reviewId) {
        createEvent(userId, EventType.REVIEW, Operation.ADD, reviewId);
    }

    public void addFriendEvent(int userId, int friendId) {
        createEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public void updateLikeEvent(int userId, int filmId) {
        createEvent(userId, EventType.LIKE, Operation.UPDATE, filmId);
    }

    public void updateReviewEvent(int userId, int reviewId) {
        createEvent(userId, EventType.REVIEW, Operation.UPDATE, reviewId);
    }

    public void updateReviewFriend(int userId, int friendId) {
        createEvent(userId, EventType.FRIEND, Operation.UPDATE, friendId);
    }


    private void createEvent(int userId, EventType eventType, Operation operation, int entityId) {
        Event event = new Event();
        Date date = new Date();
        Timestamp timestamp = Timestamp.valueOf(formatter.format(date));
        event.setTimestamp(timestamp);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setUserId(userId);
        event.setEntityId(entityId);
    }
}