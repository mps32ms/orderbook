package com.marcospaulo.orderbook.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class OrderId {

    private final UUID value;

    private OrderId(UUID value) {
        this.value = requireNonNull(value, "orderId");
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("orderId must not be blank");
        }
        try {
            return new OrderId(UUID.fromString(raw));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("orderId must be a valid UUID");
        }
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof OrderId that))
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
