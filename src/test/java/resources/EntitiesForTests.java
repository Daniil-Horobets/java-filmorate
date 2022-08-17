package resources;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.eventEnums.EventType;
import ru.yandex.practicum.filmorate.model.eventEnums.Operation;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;

public class EntitiesForTests {
    private static final Film testFilm = new Film(
            1,
            "Name",
            "Desc",
            LocalDate.now(),
            120,
            new Mpa(1, null),
            new HashSet<>(),
            new HashSet<>()
    );

    private static final User testUser = new User(
            1,
            "e@mail.com",
            "login",
            "name",
            LocalDate.now(),
            new HashSet<>()
    );

    private static final User testFriend = new User(
            2,
            "friend@mail.com",
            "friendLogin",
            "friend name",
            LocalDate.now(),
            new HashSet<>()
    );

    public static Film getTestFilm() {
        return testFilm;
    }

    public static User getTestUser() {
        return testUser;
    }

    public static User getTestFriend() {
        return testFriend;
    }

    public static Event getTestEvent() {
        return new Event(1L,
                2, Instant.now().toEpochMilli(), EventType.REVIEW, Operation.ADD, 2345);
    }
}
