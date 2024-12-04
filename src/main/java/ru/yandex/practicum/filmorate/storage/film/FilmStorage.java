package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAll();

    Film add(Film film);

    boolean isContains(long id);

    Film get(long id);

    Film update(Film film);

    boolean delete(long id);

    Film addLike(long filmId, long userId);

    Film removeLike(long filmId, long userId);
}
