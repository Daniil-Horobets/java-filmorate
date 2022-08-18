package ru.yandex.practicum.filmorate.model;

import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
//@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Mpa mpa;
    private Set<Genre> genres;
    private List<Director> directors;

    @JsonIgnore
    private Set<Integer> likedUsersIds;
}
