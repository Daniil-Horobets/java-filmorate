CREATE TABLE IF NOT EXISTS users
(
    user_id       INTEGER auto_increment,
    user_email    VARCHAR NOT NULL,
    user_login    VARCHAR NOT NULL,
    user_name     VARCHAR NOT NULL,
    user_birthday DATE    NOT NULL,
    CONSTRAINT users_user_id PRIMARY key (user_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS users_user_login_unq
    ON users (user_login);
CREATE UNIQUE INDEX IF NOT EXISTS users_user_email_unq
    ON users (user_email);
CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id   INTEGER auto_increment,
    mpa_name VARCHAR NOT NULL,
    CONSTRAINT mpa_mpa_id PRIMARY key (mpa_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS mpa_mpa_name_unq
    ON mpa (mpa_name);
CREATE TABLE IF NOT EXISTS genres
(
    genre_id   INTEGER auto_increment,
    genre_name VARCHAR NOT NULL,
    CONSTRAINT genres_genre_id PRIMARY key (genre_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS genres_genre_name_unq
    ON genres (genre_name);
CREATE TABLE IF NOT EXISTS films
(
    film_id           INTEGER auto_increment,
    film_name         VARCHAR NOT NULL,
    film_description  VARCHAR(200),
    film_release_date DATE    NOT NULL,
    film_duration     INTEGER NOT NULL,
    film_mpa_id       INTEGER NOT NULL,
    CONSTRAINT films_film_id PRIMARY key (film_id),
    CONSTRAINT films_film_mpa_id_fk FOREIGN key (film_mpa_id) REFERENCES mpa
);
CREATE TABLE IF NOT EXISTS genres_of_films
(
    genre_id INTEGER NOT NULL,
    film_id  INTEGER NOT NULL,
    CONSTRAINT genres_of_films_pk PRIMARY key (genre_id, film_id),
    CONSTRAINT genres_of_films_genre_id_fk FOREIGN key (genre_id) REFERENCES genres ON DELETE CASCADE,
    CONSTRAINT genres_of_films_film_id_fk FOREIGN key (film_id) REFERENCES films ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS likes
(
    film_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT likes_pk PRIMARY key (film_id, user_id),
    CONSTRAINT likes_film_id_fk FOREIGN key (film_id) REFERENCES films ON DELETE CASCADE,
    CONSTRAINT likes_user_id_fk FOREIGN key (user_id) REFERENCES users ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS friendship
(
    user_id        INTEGER NOT NULL,
    user_friend_id INTEGER NOT NULL,
    CONSTRAINT friendship_pk PRIMARY key (user_id, user_friend_id),
    CONSTRAINT friendship_user_id_fk FOREIGN key (user_id) REFERENCES users ON DELETE CASCADE,
    CONSTRAINT friendship_user_friend_id_fk FOREIGN key (user_friend_id) REFERENCES users ON DELETE CASCADE

create table if not exists DIRECTORS
(
    DIRECTOR_ID   INTEGER auto_increment,
    DIRECTOR_NAME CHARACTER VARYING(100) not null,
    constraint DIRECTOR_ID
        primary key (DIRECTOR_ID)
);

create table if not exists FILM_DIRECTORS
(
    FILM_ID  INTEGER not null,
    DIRECTOR_ID INTEGER not null,
    constraint FILM_DIRECTORS_PK
        primary key (FILM_ID, DIRECTOR_ID),
    constraint FOREIGN_KEY_FD_FILMS
        foreign key (FILM_ID) references films
            ON DELETE CASCADE,
    constraint FOREIGN_KEY_FD_DIRECTORS
        foreign key (DIRECTOR_ID) references DIRECTORS
            ON DELETE CASCADE
);