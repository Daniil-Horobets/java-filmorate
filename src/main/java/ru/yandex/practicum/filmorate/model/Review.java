package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import net.minidev.json.annotate.JsonIgnore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class Review {
    private final int reviewId;
    @NonNull
    private String content;
    @NonNull
    private  Boolean isPositive;
    @NonNull
    private final Integer userId;
    @NonNull
    private final Integer filmId;
    private final int useful;

    @JsonIgnore
    private Set<Integer> likedUsersIds = new HashSet<>();
    @JsonIgnore
    private Set<Integer> dislikedUsersIds = new HashSet<>();

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("review_content", content);
        values.put("review_is_positive", isPositive);
        values.put("user_id", userId);
        values.put("film_id", filmId);
        values.put("review_usefulness", useful);
        return values;
    }
}