package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewReaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage{
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Review> getAll(int count) {
        final String sqlQuery =
                "SELECT * FROM reviews ORDER BY review_usefulness DESC LIMIT ?";
        List<Review> reviews = jdbcTemplate.query(sqlQuery, this::mapToReview, count);
        for (Review review: reviews) {
            loadLikesAndDislikes(review);
        }
        return reviews;
    }

    @Override
    public List<Review> getAllByFilmId(int filmId, int count) {
        final String sqlQuery =
                "SELECT * FROM reviews WHERE film_id = ? ORDER BY review_usefulness DESC LIMIT ?";
        List<Review> reviews = jdbcTemplate.query(sqlQuery, this::mapToReview, filmId, count);
        for (Review review: reviews) {
            loadLikesAndDislikes(review);
        }
        return reviews;
    }

    @Override
    public Review get(int reviewId) {
        final String sqlQuery =
                "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sqlQuery, this::mapToReview, reviewId);
        if (reviews.size() != 1) {
            return null;
        }
        Review review = reviews.get(0);
        loadLikesAndDislikes(review);
        return review;
    }

    @Override
    public Review create(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        int id = simpleJdbcInsert.executeAndReturnKey(review.toMap()).intValue();

        return new Review(id,review.getContent(), review.getIsPositive()
                            , review.getUserId(), review.getFilmId(), 0);
    }

    @Override
    public Review update(Review review) {
        final String sqlQuery = "UPDATE reviews SET review_content = ?, review_is_positive = ? " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery
                , review.getContent()
                , review.getIsPositive()
                , review.getReviewId());
        return review;
    }

    @Override
    public void delete(int reviewId) {
        final String sqlQuery = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery, reviewId);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        final String sqlQuery = "INSERT INTO reviews_reactions (review_id, user_id, reaction_name) " +
                "VALUES (?,?,?)";
        jdbcTemplate.update(sqlQuery, reviewId, userId, ReviewReaction.LIKE.toString().toLowerCase());
        final String sqlUpdate = "UPDATE reviews SET review_usefulness = review_usefulness+1 " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlUpdate, reviewId);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        final String sqlQuery = "INSERT INTO reviews_reactions (review_id, user_id, reaction_name) " +
                "VALUES (?,?,?)";
        jdbcTemplate.update(sqlQuery, reviewId, userId, ReviewReaction.DISLIKE.toString().toLowerCase());
        final String sqlUpdate = "UPDATE reviews SET review_usefulness = review_usefulness-1 " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlUpdate, reviewId);
    }

    @Override
    public void deleteLike(int reviewId, int userId) {
        final String sqlQuery = "DELETE FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, reviewId, userId);
        final String sqlUpdate = "UPDATE reviews SET review_usefulness = review_usefulness-1 " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlUpdate, reviewId);
    }

    @Override
    public void deleteDislike(int reviewId, int userId) {
        final String sqlQuery = "DELETE FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, reviewId, userId);
        final String sqlUpdate = "UPDATE reviews SET review_usefulness = review_usefulness+1 " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlUpdate, reviewId);
    }

    private void loadLikesAndDislikes(Review review) {
        review.setLikedUsersIds(getLikedUsersIds(review.getReviewId()));
        review.setDislikedUsersIds(getDislikedUsersIds(review.getReviewId()));
    }

    private Review mapToReview(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = new Review(
        resultSet.getInt("review_id"),
        resultSet.getString("review_content"),
        resultSet.getBoolean("review_is_positive"),
        resultSet.getInt("user_id"),
        resultSet.getInt("film_id"),
        resultSet.getInt("review_usefulness")
        );
        return review;
    }

    private Set<Integer> getLikedUsersIds(int reviewId) {
        final String sqlQuery =
                "SELECT user_id FROM reviews_reactions WHERE review_id = ? AND reaction_name = ?";
        List<Integer> likedUsersIds = jdbcTemplate.query(sqlQuery
                , (resultSet, rowNum) -> (resultSet.getInt("user_id"))
                , reviewId, ReviewReaction.LIKE.toString().toLowerCase());
        return new HashSet<>(likedUsersIds);
    }

    private Set<Integer> getDislikedUsersIds(int reviewId) {
        final String sqlQuery =
                "SELECT user_id FROM reviews_reactions WHERE review_id = ? AND reaction_name = ?";
        List<Integer> likedUsersIds = jdbcTemplate.query(sqlQuery
                , (resultSet, rowNum) -> (resultSet.getInt("user_id"))
                , reviewId, ReviewReaction.DISLIKE.toString().toLowerCase());
        return new HashSet<>(likedUsersIds);
    }
}
