package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.eventEnums.EventType;
import ru.yandex.practicum.filmorate.model.eventEnums.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;

@Service
public class EventService {
    //TODO добавить методы к функциональности review

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

    public Event removeLikeEvent(int userId, int filmId) {
        return createEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public Event removeReviewEvent(int userId, int reviewId) {
        return createEvent(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public Event removeFriendEvent(int userId, int friendId) {
        return createEvent(userId, EventType.REVIEW, Operation.REMOVE, friendId);
    }

    public Event addLikeEvent(int userId, int filmId) {
        return createEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public Event addReviewEvent(int userId, int reviewId) {
        return createEvent(userId, EventType.REVIEW, Operation.ADD, reviewId);
    }

    public Event addFriendEvent(int userId, int friendId) {
        return createEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public Event updateLikeEvent(int userId, int filmId) {
        return createEvent(userId, EventType.LIKE, Operation.UPDATE, filmId);
    }

    public Event updateReviewEvent(int userId, int reviewId) {
        return createEvent(userId, EventType.REVIEW, Operation.UPDATE, reviewId);
    }

    public Event updateReviewFriend(int userId, int friendId) {
        return createEvent(userId, EventType.FRIEND, Operation.UPDATE, friendId);
    }


    private Event createEvent(int userId, EventType eventType, Operation operation, int entityId) {
        Event event = new Event();
        event.setTimestamp((Instant.now().toEpochMilli()));
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setUserId(userId);
        event.setEntityId(entityId);
        return eventStorage.createEvent(event);
    }
}