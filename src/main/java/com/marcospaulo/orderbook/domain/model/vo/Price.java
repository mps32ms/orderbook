package com.marcospaulo.orderbook.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class Price implements Comparable<Price> {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal value;

    private Price(BigDecimal value) {
        this.value = normalize(requireNonNull(value, "price"));
        if (this.value.signum() <= 0) {
            throw new DomainException("price must be > 0");
        }
    }

    public static Price of(BigDecimal value) {
        return new Price(value);
    }

    public static Price of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("price must not be blank");
        }
        try {
            return new Price(new BigDecimal(raw));
        } catch (NumberFormatException ex) {
            throw new DomainException("price must be a valid decimal");
        }
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public int compareTo(Price other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Price that))
            return false;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    private static BigDecimal normalize(BigDecimal v) {
        return v.setScale(SCALE, ROUNDING);
    }

    private static BigDecimal requireNonNull(BigDecimal v, String field) {
        if (v == null)
            throw new DomainException(field + " must not be null");
        return v;
    }

}
