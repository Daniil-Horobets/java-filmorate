package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> getAll(int count);
    List<Review> getAllByFilmId(int filmId, int count);
    Review get(int reviewId);
    Review create(Review review);
    Review update(Review review);
    void delete(int reviewId);
    void addLike(int reviewId, int userId);
    void addDislike(int reviewId, int userId);
    void deleteLike(int reviewId, int userId);
    void deleteDislike(int reviewId, int userId);
}
