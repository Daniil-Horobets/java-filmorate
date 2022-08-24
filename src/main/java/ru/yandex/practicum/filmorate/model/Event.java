package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "entityId")
public class Event {
    private Long eventId;
    private int userId;
    private Long timestamp;
    private EventType eventType;
    private Operation operation;
    private int entityId;
}