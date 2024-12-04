package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    @Autowired
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        log.info("Фильм из запроса при создании - {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            log.info("Название фильма не должно быть пустым - {}", film.getName());
            throw new ValidationException("Название фильма не должно быть пустым ");
        }
        descriptionLengthValidate(film);
        dateReleaseValidate(film);
        durationValidate(film);
        film.setUsersWhoLike(new HashSet<>());
        return filmStorage.add(film);
    }

    public Film update(Film film) {

        log.info("Фильм из тела PUT запроса - {}", film);

        if (film.getId() == null) {
            log.info("Не указан id фильма");
            throw new ValidationException("Не указан id фильма: id=" + film.getId());
        }

        if (film.getId() < 0) {
            log.info("Указан отрицательный id фильма {}", film.getId());
            throw new ValidationException("Указан отрицательный id фильма: id=" + film.getId());
        }

        if (!filmStorage.isContains(film.getId())) {
            log.info("Неверно указан id фильма - {}", film.getId());
            throw new NotFoundException("Фильма с данным id не существует: id=" + film.getId());
        }

        Film oldFilm = filmStorage.get(film.getId());
        Film newFilm = buildNewFilm(film, oldFilm);

        log.info("Фильм перед сохранением при изменении - {}", newFilm);
        return filmStorage.update(newFilm);
    }


    public Film addLike(long filmId, long userId) {
        log.info("Значения id при добавлении лайка: filmId={}, userId={}", filmId, userId);
        checkContainsUserAndFilm(userId, filmId);
        likeValidate(filmId, userId);

        if (filmStorage.get(filmId).getUsersWhoLike().contains(userId)) {
            throw new ValidationException("Пользователь с этим id уже поставил лайк: userId=" + userId);
        }
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(long filmId, long userId) {
        log.info("Значения id при удалении лайка: filmId={}, userId={}", filmId, userId);
        checkContainsUserAndFilm(userId, filmId);
        likeValidate(filmId, userId);

        if (!filmStorage.get(filmId).getUsersWhoLike().contains(userId)) {
            throw new ValidationException("Пользователь с этим id ещё не поставил лайк: userId=" + userId);
        }
        return filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Значение count должно быть положительным числом: count=" + count);
        }
        return filmStorage.getAll().stream()
                .sorted(this::comparatorByPopularity)
                .limit(count)
                .collect(Collectors.toList());
    }


    private Film buildNewFilm(Film newFilm, Film oldFilm) {
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            newFilm.setName(oldFilm.getName());
        }
        if (newFilm.getDescription() == null || newFilm.getDescription().isBlank()) {
            newFilm.setDescription(oldFilm.getDescription());
        } else {
            descriptionLengthValidate(newFilm);
        }
        if (newFilm.getReleaseDate() == null) {
            newFilm.setReleaseDate(oldFilm.getReleaseDate());
        } else {
            dateReleaseValidate(newFilm);
        }
        if (newFilm.getDuration() == null) {
            newFilm.setDuration(oldFilm.getDuration());
        } else {
            durationValidate(newFilm);
        }
        newFilm.setUsersWhoLike(oldFilm.getUsersWhoLike());
        return newFilm;
    }

    private void checkContainsUserAndFilm(long userId, long filmId) {
        if (!userStorage.isContains(userId)) {
            throw new NotFoundException("Пользователя с таким id не существует: userId=" + userId);
        }
        if (!filmStorage.isContains(filmId)) {
            throw new NotFoundException("Фильма с таким id не существует: filmId=" + filmId);
        }
    }

    private void likeValidate(long filmId, long userId) {
        if (filmId < 0) {
            throw new ValidationException("id фильма не может быть отрицательным числом: filmId=" + filmId);
        }
        if (!userStorage.isContains(userId)) {
            throw new ValidationException("id пользователя не может быть отрицательным числом: userId=" + userId);
        }
        if (!filmStorage.isContains(filmId)) {
            throw new ValidationException("Фильма с таким id не существует: filmId=" + filmId);
        }
        if (!userStorage.isContains(userId)) {
            throw new ValidationException("Пользователя с таким id не существует: userId=" + userId);
        }
    }

    private void durationValidate(Film film) {
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть положительным числом - {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом: duration=" + film.getDuration());
        }
    }

    private void dateReleaseValidate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.info("Дата релиза должна быть не раньше {} - {}", MIN_RELEASE_DATE, film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше " + MIN_RELEASE_DATE + ": releaseDate="
                    + film.getReleaseDate());
        }
    }

    private void descriptionLengthValidate(Film film) {
        if (film.getDescription().length() > 200) {
            log.info("Длина описания фильма не может быть больше {} символов - {}",
                    MAX_DESCRIPTION_LENGTH, film.getDescription().length());
            throw new ValidationException("Длина описания фильма не может быть больше "
                    + MAX_DESCRIPTION_LENGTH + " символов: descriptionLength=" + film.getDescription().length());
        }
    }

    private int comparatorByPopularity(Film film1, Film film2) {
        if (film2.getUsersWhoLike() == null) return -1;
        if (film1.getUsersWhoLike() == null) return 1;
        return Integer.compare(film2.getUsersWhoLike().size(), film1.getUsersWhoLike().size());
    }

}
