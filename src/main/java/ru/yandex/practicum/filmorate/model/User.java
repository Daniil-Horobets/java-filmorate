package ru.yandex.practicum.filmorate.model;

import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    @JsonIgnore
    private Set<Integer> friendsIds = new HashSet<>();
}
