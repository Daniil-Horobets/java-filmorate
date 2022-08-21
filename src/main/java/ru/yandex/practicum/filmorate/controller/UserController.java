package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @GetMapping
    public List<User> findAll() {
        log.info("Request endpoint: 'GET /users'");
        return userService.getAll();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Request endpoint: 'POST /users'");
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Request endpoint: 'PUT /users'");
        return userService.update(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        log.info("Request endpoint: 'GET /users/{}'", id);
        return userService.getById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Request endpoint: 'PUT /users/{}/friends/{}'", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Request endpoint: 'DELETE /users/{}/friends/{}'", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("Request endpoint: 'GET /users/{}/friends'", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations (@PathVariable Integer id) {
        log.info("Request endpoint: 'GET /users/{}/recommendations'", id);
        return userService.getRecommendations(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Request endpoint: 'GET /users/{}/friends/common/{}'", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") int id) {
        final boolean deleted = userService.delete(id);
        log.info("Request endpoint: 'DELETE /films/{}'", id);

        return deleted
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserEvents(@PathVariable int id) {
        log.info("Request endpoint: 'GET /users/{}/feed'", id);
        return eventService.getUserEvents(id);
    }
}
