package com.marcospaulo.orderbook.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class TradeId {

    private final UUID value;

    private TradeId(UUID value) {
        this.value = requireNonNull(value, "tradeId");
    }

    public static TradeId of(UUID value) {
        return new TradeId(value);
    }

    public static TradeId newId() {
        return new TradeId(UUID.randomUUID());
    }

    public static TradeId fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("tradeId must not be blank");
        }
        try {
            return new TradeId(UUID.fromString(raw));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("tradeId must be a valid UUID");
        }
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof TradeId that))
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
