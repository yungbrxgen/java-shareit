package ru.practicum.shareit.booking.model;

import java.util.Optional;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingState> from(String stateStr) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stateStr)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
