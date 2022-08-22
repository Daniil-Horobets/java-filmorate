package resources;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntitiesForTests {
    private static final Film testFilm = new Film(
            1,
            "FilmName",
            "Desc",
            LocalDate.of(1998, 11 , 5),
            120,
            new Mpa(1, null),
            new HashSet<>(Set.of(new Genre(1,null))),
            new ArrayList<>(List.of(new Director(1, null))),
            null
    );

    private static final Film testFilm2 = new Film(
            2,
            "FilmName2",
            "Desc2",
            LocalDate.of(1998, 10 , 5),
            120,
            new Mpa(1, null),
            new HashSet<>(Set.of(new Genre(2,null))),
            new ArrayList<>(List.of(new Director(1, null))),
            null
    );

    private static final Film testFilm3 = new Film(
            3,
            "FilmName3",
            "Desc3",
            LocalDate.of(2008, 9 , 5),
            120,
            new Mpa(1, null),
            new HashSet<>(Set.of(new Genre(1,null))),
            new ArrayList<>(List.of(new Director(2, null))),
            null
    );

    private static final Film testFilm4 = new Film(
            4,
            "FilmName4",
            "Desc4",
            LocalDate.of(2008, 7 , 5),
            120,
            new Mpa(1, null),
            new HashSet<>(Set.of(new Genre(2,null))),
            new ArrayList<>(List.of(new Director(2, null))),
            null
    );

    private static final Film testFilm5 = new Film(
            5,
            "FilmName5",
            "Desc5",
            LocalDate.of(2008, 1 , 5),
            120,
            new Mpa(1, null),
            new HashSet<>(Set.of(new Genre(1,null))),
            new ArrayList<>(List.of(new Director(1, null))),
            null
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

    private static final User testUser2 = new User(
            3,
            "e3@mail.com",
            "login3",
            "name3",
            LocalDate.now(),
            new HashSet<>()
    );

    private static final User testUser3 = new User(
            4,
            "e4@mail.com",
            "login4",
            "name4",
            LocalDate.now(),
            new HashSet<>()
    );

    private static final Director testDirector = new Director(
            1,
            "DirectorName1"
    );

    private static final Director testDirector2 = new Director(
            2,
            "DirectorName2"
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

    public static Film getTestFilm2() { return testFilm2;
    }
    public static Film getTestFilm3() { return testFilm3;
    }
    public static Film getTestFilm4() { return testFilm4;
    }
    public static Film getTestFilm5() { return testFilm5;
    }

    public static User getTestUser2() {
        return testUser2;
    }
    public static User getTestUser3() {
        return testUser3;
    }
    public static Director getTestDirector() {
        return testDirector;
    }
    public static Director getTestDirector2() {
        return testDirector2;
    }
}