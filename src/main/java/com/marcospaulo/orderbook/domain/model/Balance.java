package com.marcospaulo.orderbook.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class Balance {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private BigDecimal available;
    private BigDecimal reserved;

    private Balance(BigDecimal available, BigDecimal reserved) {
        this.available = normalize(nonNull(available, "available"));
        this.reserved = normalize(nonNull(reserved, "reserved"));
        if (this.available.signum() < 0)
            throw new DomainException("available must be >= 0");
        if (this.reserved.signum() < 0)
            throw new DomainException("reserved must be >= 0");
    }

    public static Balance of(BigDecimal available, BigDecimal reserved) {
        return new Balance(available, reserved);
    }

    public BigDecimal available() {
        return available;
    }

    public BigDecimal reserved() {
        return reserved;
    }

    public void reserve(BigDecimal amount) {
        BigDecimal a = normalize(nonNull(amount, "amount"));
        if (a.signum() <= 0)
            throw new DomainException("reserve amount must be > 0");
        if (available.compareTo(a) < 0)
            throw new DomainException("insufficient available balance to reserve");
        available = available.subtract(a);
        reserved = reserved.add(a);
    }

    public void release(BigDecimal amount) {
        BigDecimal a = normalize(nonNull(amount, "amount"));
        if (a.signum() <= 0)
            throw new DomainException("release amount must be > 0");
        if (reserved.compareTo(a) < 0)
            throw new DomainException("insufficient reserved balance to release");
        reserved = reserved.subtract(a);
        available = available.add(a);
    }

    public void debitReserved(BigDecimal amount) {
        BigDecimal a = normalize(nonNull(amount, "amount"));
        if (a.signum() <= 0)
            throw new DomainException("debit amount must be > 0");
        if (reserved.compareTo(a) < 0)
            throw new DomainException("insufficient reserved balance to debit");
        reserved = reserved.subtract(a);
    }

    public void creditAvailable(BigDecimal amount) {
        BigDecimal a = normalize(nonNull(amount, "amount"));
        if (a.signum() <= 0)
            throw new DomainException("credit amount must be > 0");
        available = available.add(a);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Balance that))
            return false;
        return available.compareTo(that.available) == 0 && reserved.compareTo(that.reserved) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(available.stripTrailingZeros(), reserved.stripTrailingZeros());
    }

    private static BigDecimal normalize(BigDecimal v) {
        return v.setScale(SCALE, ROUNDING);
    }

    private static BigDecimal nonNull(BigDecimal v, String field) {
        if (v == null)
            throw new DomainException(field + " must not be null");
        return v;
    }

}
