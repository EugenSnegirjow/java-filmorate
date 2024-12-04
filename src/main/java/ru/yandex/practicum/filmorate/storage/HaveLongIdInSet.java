package ru.yandex.practicum.filmorate.storage;

import java.util.Set;

public interface HaveLongIdInSet {

    default long getNextId(Set<Long> keySet) {
        long currentMaxId = keySet.stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
