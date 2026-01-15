package com.marcospaulo.orderbook.domain.model.vo;

import java.util.Objects;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class Quantity {

    private final long value;

    private Quantity(long value) {
        if (value < 0)
            throw new DomainException("quantity must be >= 0");
        this.value = value;
    }

    public static Quantity ofPositive(long value) {
        if (value <= 0)
            throw new DomainException("quantity must be > 0");
        return new Quantity(value);
    }

    public static Quantity ofNonNegative(long value) {
        return new Quantity(value);
    }

    public long value() {
        return value;
    }

    public boolean isZero() {
        return value == 0;
    }

    public Quantity minus(Quantity other) {
        if (other == null)
            throw new DomainException("quantity to subtract must not be null");
        long result = this.value - other.value;
        if (result < 0)
            throw new DomainException("quantity cannot be negative");
        return new Quantity(result);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Quantity that))
            return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
