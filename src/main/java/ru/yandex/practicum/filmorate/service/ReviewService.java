package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotUniqueReactionException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

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

    public void addLike(int reviewId, int userId) {
        checkReviewExistence(reviewId);
        userService.checkUserExistence(userId, userStorage);
        checkReactionUserIdUnique(reviewId, userId);
        reviewStorage.addLike(reviewId,userId);
    }

    public void deleteLike(int reviewId, int userId){
        checkReviewExistence(reviewId);
        userService.checkUserExistence(userId, userStorage);
        checkLikeUserIdPresent(reviewId, userId);
        reviewStorage.deleteLike(reviewId,userId);
    }

    public void addDislike(int reviewId, int userId){
        checkReviewExistence(reviewId);
        userService.checkUserExistence(userId, userStorage);
        checkReactionUserIdUnique(reviewId, userId);
        reviewStorage.addDislike(reviewId,userId);
    }

    public void deleteDislike(int reviewId, int userId){
        checkReviewExistence(reviewId);
        userService.checkUserExistence(userId, userStorage);
        checkDislikeUserIdPresent(reviewId, userId);
        reviewStorage.deleteDislike(reviewId,userId);
    }

    public void checkReviewExistence(int reviewId) {
        getById(reviewId);
    }

    public void checkReactionUserIdUnique(int reviewId, int userId){
        Review review = getById(reviewId);
        if (review.getLikedUsersIds().contains(userId) || review.getDislikedUsersIds().contains(userId)) {
            throw new NotUniqueReactionException("User with id=" + userId
                    + " has already added reaction to review with id=" + reviewId);
        }
    }

    public void checkLikeUserIdPresent(int reviewId, int userId){
        Review review = getById(reviewId);
        if (!review.getLikedUsersIds().contains(userId)) {
            throw new NotFoundException("User with id=" + userId
                    + " didn't add like to review with id=" + reviewId);
        }
    }

    public void checkDislikeUserIdPresent(int reviewId, int userId){
        Review review = getById(reviewId);
        if (!review.getDislikedUsersIds().contains(userId)) {
            throw new NotFoundException("User with id=" + userId
                    + " didn't add like to review with id=" + reviewId);
        }
    }
}
