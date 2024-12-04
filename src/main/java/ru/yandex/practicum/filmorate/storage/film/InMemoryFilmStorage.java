package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.HaveLongIdInSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage, HaveLongIdInSet {
    private static final Map<Long, Film> films = new HashMap<>();


    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film add(Film film) {
        long nextId = getNextId(films.keySet());
        film.setId(nextId);
        films.put(nextId, film);
        log.info("Фильм перед сохранением при создании - {}", film);
        return film;
    }

    @Override
    public boolean isContains(long id) {
        return films.containsKey(id);
    }

    @Override
    public Film get(long id) {
        return films.get(id);
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film addLike(long filmId, long userId) {
        films.get(filmId).getUsersWhoLike().add(userId);
        return update(films.get(filmId));
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        films.get(filmId).getUsersWhoLike().remove(userId);
        return update(films.get(filmId));
    }

    @Override
    public boolean delete(long id) {
        return false;
    }
}
