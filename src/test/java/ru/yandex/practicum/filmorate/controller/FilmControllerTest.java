package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.filmorate.service.FilmService.MAX_DESCRIPTION_LENGTH;
import static ru.yandex.practicum.filmorate.service.FilmService.MIN_RELEASE_DATE;

class FilmControllerTest {

    private FilmController controller;

    private Film film0;
    private Film film1;
    private Film film2;

    @BeforeEach
    public void createController() {
        controller = new FilmController(
                new FilmService(
                        new InMemoryFilmStorage(),
                        new InMemoryUserStorage()
                )
        );

        film0 = Film.builder()
                .name("Побег из Шоушенка")
                .description("По нелепому стечению обстоятельств успешный бизнесмен Энди")
                .duration(142)
                .releaseDate(LocalDate.of(1994, 3, 25))
                .build();
        film1 = Film.builder()
                .name("Крестный отец")
                .description("1945 год. Вокруг роскошного особняка Вито Корлеоне " +
                        "с самого утра творилось настоящее столпотворение.")
                .duration(175)
                .releaseDate(LocalDate.of(1972, 5, 28))
                .build();
        film2 = Film.builder()
                .name("12 разгневанных мужчин")
                .description("Снятый в 1957 году, этот фильм по прежнему остается в числе лучших криминальных драм.")
                .duration(96)
                .releaseDate(LocalDate.of(1957, 1, 5))
                .build();

        controller.create(film0);
        controller.create(film1);
        controller.create(film2);
    }

    @Disabled
    @Test
    void getAllFilms() {
        Collection<Film> expected = new ArrayList<>() {{
            add(film0.toBuilder().id(1L).build());
            add(film1.toBuilder().id(2L).build());
            add(film2.toBuilder().id(3L).build());
        }};
        Collection<Film> actual = controller.getAllFilms();
        assertEquals(expected.toString(), actual.toString(), "Возвращён неверный список фильмов");
    }

    @Test
    void createFilmWithDateEqualsMinDate() {
        Film filmWithDateEqualsMinDate = Film.builder()
                .name("Первый фильм")
                .description("Поезд")
                .duration(4)
                .releaseDate(MIN_RELEASE_DATE)
                .build();

        Film actual = controller.create(filmWithDateEqualsMinDate);
        filmWithDateEqualsMinDate.setId(4L);
        assertEquals(filmWithDateEqualsMinDate, actual, "Неверно добавлен или не добавлен " +
                "фильм с минимальной датой");
    }

    @Test
    void createFilmWithDateAfterMinDate() {
        Film filmWithDateAfterMinDate = Film.builder()
                .name("Второй фильм")
                .description("2 поезда")
                .duration(8)
                .releaseDate((LocalDate.from(MIN_RELEASE_DATE)).plusDays(1))
                .build();

        Film actual = controller.create(filmWithDateAfterMinDate);
        filmWithDateAfterMinDate.setId(4L);
        assertEquals(filmWithDateAfterMinDate, actual, "Неверно добавлен или не добавлен " +
                "фильм с датой больше минимальной");
    }

    @Test
    void createFilmWithDateBeforeMinDate() {
        Film filmWithDateBeforeMinDate = Film.builder()
                .name("Очень старый фильм")
                .description("Фильм снятый до изобретения синематографа")
                .duration(100)
                .releaseDate(LocalDate.from(MIN_RELEASE_DATE).minusDays(1))
                .build();
        ValidationException actual = assertThrows(
                ValidationException.class,
                () -> controller.create(filmWithDateBeforeMinDate)
        );

        assertEquals("Дата релиза должна быть не раньше " + MIN_RELEASE_DATE + ": releaseDate=1895-12-27", actual.getMessage());
    }

    @Test
    void createFlmWithDescriptionLength200() {
        Film filmWithDescriptionLength200 = Film.builder()
                .name("Фильм с максимальным описанием")
                .description("1945 год. Вокруг роскошного особняка Вито Корлеоне " +
                        "с самого утра творилось настоящее столпотворение. " +
                        "Сегодня крестный отец итальянской мафии выдавал замуж свою единственную дочь Конни. " +
                        "На пышном банке")
                .duration(60)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();

        Film actual = controller.create(filmWithDescriptionLength200);
        filmWithDescriptionLength200.setId(4L);
        assertEquals(filmWithDescriptionLength200, actual, "Неверно добавлен или не добавлен " +
                "фильм с максимальной длиной описания");

    }

    @Test
    void createFilmWithDescriptionLength201() {
        Film filmWithDescriptionLength201 = Film.builder()
                .name("Фильм с максимальным описанием")
                .description("1945 год. Вокруг роскошного особняка Вито Корлеоне " +
                        "с самого утра творилось настоящее столпотворение. " +
                        "Сегодня крестный отец итальянской мафии выдавал замуж свою единственную дочь Конни. " +
                        "На пышном банкет")
                .duration(60)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();

        ValidationException actual = assertThrows(ValidationException.class,
                () -> controller.create(filmWithDescriptionLength201)
        );
        assertEquals("Длина описания фильма не может быть больше "
                        + MAX_DESCRIPTION_LENGTH + " символов: descriptionLength=201", actual.getMessage(),
                "Неверно добавляется фильм с длинным описанием");
    }

    @Test
    void updateWithRightId() {
        Film updateForFilm0 = Film.builder()
                .id(1L)
                .description("По нелепому стечению обстоятельств успешный бизнесмен Энди Дюфрейн " +
                        "обвиняется в убийстве жены. Суд приговаривает его к пожизненному сроку.")
                .build();
        Film actual = controller.update(updateForFilm0);
        Film expected = film0.toBuilder()
                .id(1L)
                .description("По нелепому стечению обстоятельств успешный бизнесмен Энди Дюфрейн " +
                        "обвиняется в убийстве жены. Суд приговаривает его к пожизненному сроку.")
                .build();
        assertEquals(expected, actual, "Фильм обновляется неверно");

    }

    @Test
    void updateWithWrongId() {
        Film wrongIdFilm = Film.builder()
                .id(777L)
                .name("nisi eiusmod")
                .description("adipisicing")
                .duration(100)
                .releaseDate(LocalDate.of(1967, 3, 25))
                .build();

        NotFoundException actual = assertThrows(
                NotFoundException.class,
                () -> controller.update(wrongIdFilm)
        );
        assertEquals("Фильма с данным id не существует: id=777", actual.getMessage(),
                "Неверно обрабатывается обновление фильма с неверным id");
    }

    @Test
    void updateWithVoidId() {
        Film voidIdFilm = Film.builder()
                .name("nisi eiusmod")
                .description("adipisicing")
                .duration(100)
                .releaseDate(LocalDate.of(1967, 3, 25))
                .build();

        ValidationException actual = assertThrows(
                ValidationException.class,
                () -> controller.update(voidIdFilm)
        );
        assertEquals("Не указан id фильма: id=null", actual.getMessage(),
                "Неверно обрабатывается обновление фильма с пустым id");
    }
}