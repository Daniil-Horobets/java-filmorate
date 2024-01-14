# Filmorate - Movie Rating and Social Platform

Filmorate is an evolving platform designed to streamline movie choices and enhance social interactions around shared film interests. This project leverages the Spring Boot framework and integrates functionalities for movie management, user ratings, community building, and more.

## Features

### 1. Movie Management

Filmorate allows users to add, update, and retrieve movies easily. Key details of each movie include:

- Movie ID
- Title
- Description
- Release Date
- Duration
- Director

### 2. User Profiles

Users have dedicated profiles with essential information:

- User ID
- Email
- Login
- Display Name
- Birthday

### 3. Community Building

The platform facilitates community building through features like:

- Adding friends
- Liking movies
- Retrieving common friends

### 4. Rating and Recommendations

Filmorate calculates movie ratings based on user reviews and provides a curated list of top 5 recommended movies for users to discover.

### 5. Movie Directors

Movies now include information about the director. This introduces the following functionalities:

- Retrieve all movies by a director, sorted by likes or release year.
- Improved API for adding movies, including director information.

### 6. Top Movies by Genre and Year

Users can now retrieve the top N movies based on likes for a specific genre and year.

### 7. Activity Feed

A new feature, the "Activity Feed," allows users to view recent events on the platform, such as friend requests, removal from friends, likes, and reviews from friends.

### 8. Deletion of Movies and Users

Added functionality to delete movies and users by their respective IDs.

### 9. Common Movies with Friends

Users can now view a list of common movies with a friend, sorted by popularity.

### 10. Search Functionality

A search feature has been implemented to search for movies by title and director. The algorithm supports substring searches.

### 11. Reviews

Users can now leave reviews for movies, including a rating, usefulness, and type (positive/negative). Reviews are sortable by usefulness rating.

### 12. Recommendations

A basic recommender system suggests movies based on the user's likes and the likes of users with similar tastes.

## Project Structure

### Models

- `Film`: Represents movie details.
- `User`: Represents user information.
- `Director`: Represents director information.
- `Review`: Represents user reviews for movies.

### Controllers

- `FilmController`: Manages movie-related endpoints.
- `UserController`: Manages user-related endpoints.
- `DirectorController`: Manages director-related endpoints.
- `ReviewController`: Manages review-related endpoints.

### Services

- `UserService`: Manages user-specific operations like adding friends and retrieving common friends.
- `FilmService`: Manages movie-specific operations such as liking movies and retrieving popular films.
- `DirectorService`: Manages director-related operations.
- `ReviewService`: Manages review-related operations.

### Storage

- `InMemoryFilmStorage`, `InMemoryUserStorage`, `InMemoryDirectorStorage`, `InMemoryReviewStorage`: Initial implementations for in-memory data storage.
- `FilmDbStorage`, `UserDbStorage`, `DirectorDbStorage`, `ReviewDbStorage`: Implementations for data access objects (DAO) handling persistent data in a database.

## Database Integration

The project integrates an H2 embedded database, ensuring data persistence across application restarts. The database structure is defined in `schema.sql`, with relevant data initialization in `data.sql`.

### Database Entity Relationship Diagram:

![](filmorate.png)

Database source: `src/main/resources/schema.sql`

Database datasource: `src/main/resources/data.sql`

## Testing

Integration testing is implemented to verify the functionality of DAO objects with the database. This ensures proper data access and manipulation.

## Dependencies

- Spring Boot
- H2 Database
- Lombok
- SLF4J for logging

## How to Run

Ensure the correct dependencies are installed, and run the Spring Boot application. The application will be accessible through specified endpoints for movie and user operations.

### Endpoints

#### Movie-related Endpoints

- `POST /films`: Add a new movie.
- `PUT /films/{id}`: Update movie details.
- `GET /films`: Retrieve all movies.
- `GET /films/director/{directorId}?sortBy=[year,likes]`: Retrieve movies by director, sorted by likes or release year.

#### User-related Endpoints

- `POST /users`: Create a new user.
- `PUT /users/{id}`: Update user details.
- `GET /users`: Retrieve all users.
- `GET /users/{id}/feed`: Retrieve user activity feed.
- `DELETE /users/{userId}`: Delete a user.

#### Community-related Endpoints

- `PUT /users/{id}/friends/{friendId}`: Add a friend.
- `DELETE /users/{id}/friends/{friendId}`: Remove a friend.
- `GET /users/{id}/friends`: Retrieve a list of friends.
- `GET /users/{id}/friends/common/{otherId}`: Retrieve common friends.
- `GET /films/common?userId={userId}&friendId={friendId}`: Retrieve common movies with a friend, sorted by popularity.

#### Movie Rating Endpoints

- `PUT /films/{id}/like/{userId}`: Add a like to a movie.
- `DELETE /films/{id}/like/{userId}`: Remove a like from a movie.
- `GET /films/popular?count={count}&genreId={genreId}&year={year}`: Retrieve popular movies based on genre and year.

#### Search Endpoint

- `GET /films/search?query={query}&by={director,title}`: Search for movies by title or director.

#### Review-related Endpoints

- `POST /reviews`: Add a new review.
- `PUT /reviews`: Update an existing review.
- `DELETE /reviews/{id}`: Delete a review.
- `GET /reviews/{id}`: Retrieve a review by ID.
- `GET /reviews?filmId={filmId}&count={count}`: Retrieve reviews for a movie.

#### Recommendation Endpoint

- `GET /users/{id}/recommendations`: Retrieve movie recommendations for a user.


Happy movie watching and socializing! 🎞️