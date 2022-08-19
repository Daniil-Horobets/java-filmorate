package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Review create(@RequestBody Review review) {
        log.info("Request endpoint: 'POST /reviews'");
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        log.info("Request endpoint: 'PUT /reviews'");
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Request endpoint: 'DELETE /reviews/{}'", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable int id) {
        log.info("Request endpoint: 'GET /reviews/{}'", id);
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(defaultValue = "0") int filmId,
                                   @RequestParam(defaultValue = "10") int count) {
        log.info("Request endpoint: 'GET /reviews?filmId={}&count={}'", filmId, count);
        return reviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'PUT /reviews/{}/like/{}'", id, userId);
        reviewService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'DELETE /reviews/{}/like/{}'", id, userId);
        reviewService.deleteLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'PUT /reviews/{}/dislike/{}'", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Request endpoint: 'DELETE /reviews/{}/dislike/{}'", id, userId);
        reviewService.deleteDislike(id, userId);
    }
}
