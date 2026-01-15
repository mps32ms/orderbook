package com.marcospaulo.orderbook.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        this.value = requireNonNull(value, "userId");
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("userId must not be blank");
        }
        try {
            return new UserId(UUID.fromString(raw));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("userId must be a valid UUID");
        }
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof UserId that))
            return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private static UUID requireNonNull(UUID v, String field) {
        if (v == null)
            throw new DomainException(field + " must not be null");
        return v;
    }
}
