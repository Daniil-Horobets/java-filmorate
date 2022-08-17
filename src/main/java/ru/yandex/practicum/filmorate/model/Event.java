package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.eventEnums.EventType;
import ru.yandex.practicum.filmorate.model.eventEnums.Operation;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    Long eventId;
    int userId;
    Timestamp timestamp;
    EventType eventType;
    Operation operation;
    int entityId;
}