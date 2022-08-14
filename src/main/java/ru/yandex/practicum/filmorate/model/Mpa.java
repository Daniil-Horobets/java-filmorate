package ru.yandex.practicum.filmorate.model;

import lombok.*;

//@Data не совсем подходит, т.к. @EqualsAndHashCode определены параметром id
@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Mpa {
    private final int id;
    private final String name;
}
