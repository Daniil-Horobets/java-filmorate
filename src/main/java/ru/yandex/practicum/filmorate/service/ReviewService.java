package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class ReviewService {
    @Qualifier("reviewDbStorage")
    @Autowired
    private ReviewStorage reviewStorage;

    @Qualifier("filmDbStorage")
    @Autowired
    private FilmStorage filmStorage;

    @Qualifier("userDbStorage")
    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    public Review create(Review review){
        filmService.checkFilmExistence(review.getFilmId(), filmStorage);
        userService.checkUserExistence(review.getUserId(), userStorage);
        return reviewStorage.create(review);
    }

    public Review update(Review review){
        checkReviewExistence(review.getReviewId());
        return reviewStorage.update(review);
    }

    public void delete(int reviewId){
        checkReviewExistence(reviewId);
        reviewStorage.delete(reviewId);
    }

    public Review getById(int reviewId){
        Review review = reviewStorage.get(reviewId);
        if (review == null) {
            throw new NotFoundException("Review with id=" + reviewId + " not found");
        }
        return review;
    }

    public List<Review> getAll(int filmId, int count){
        List<Review> reviews;
        if (filmId == 0) {
            reviews = reviewStorage.getAll(count);
        } else {
            filmService.checkFilmExistence(filmId, filmStorage);
            reviews = reviewStorage.getAllByFilmId(filmId, count);
        }
        return reviews;
    }

    public void addReactionAssessment(int reviewId, int userId, boolean isLike) {
        checkReviewExistence(reviewId);
        userService.checkUserExistence(userId, userStorage);
        checkReactionUserIdUnique(reviewId, userId);
        reviewStorage.addReactionAssessment(reviewId, userId, isLike);
    }

    public void deleteReactionAssessment(int reviewId, int userId, boolean isLike) {
        checkReviewExistence(reviewId);
        userService.checkUserExistence(userId, userStorage);
        checkReactionUserIdPresent(reviewId, userId, isLike);
        reviewStorage.deleteReactionAssessment(reviewId, userId, isLike);
    }

    public void checkReviewExistence(int reviewId) {
        getById(reviewId);
    }

    public void checkReactionUserIdUnique(int reviewId, int userId){
        Review review = getById(reviewId);
        if (review.getLikedUsersIds().contains(userId) || review.getDislikedUsersIds().contains(userId)) {
            throw new AlreadyExistsException("User with id=" + userId
                    + " has already added reaction to review with id=" + reviewId);
        }
    }

    public void checkReactionUserIdPresent(int reviewId, int userId, boolean isLike){
        Review review = getById(reviewId);
        if (isLike) {
            if (!review.getLikedUsersIds().contains(userId)) {
                throw new NotFoundException("User with id=" + userId
                        + " didn't add like to review with id=" + reviewId);
            }
        } else {
            if (!review.getDislikedUsersIds().contains(userId)) {
                throw new NotFoundException("User with id=" + userId
                        + " didn't add dislike to review with id=" + reviewId);
            }
        }
    }
}
