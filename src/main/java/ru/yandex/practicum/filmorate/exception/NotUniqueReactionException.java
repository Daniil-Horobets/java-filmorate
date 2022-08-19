package ru.yandex.practicum.filmorate.exception;

public class NotUniqueReactionException extends RuntimeException{
    public NotUniqueReactionException(final String message) {
        super(message);
    }
}
