package com.marcospaulo.orderbook.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.OrderId;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public final class Order {

    private final OrderId id;
    private final UserId userId;
    private final Side side;
    private final Price price;
    private final Instant createdAt;

    private final Quantity originalQty;
    private Quantity remainingQty;

    private Order(
            OrderId id,
            UserId userId,
            Side side,
            Price price,
            Quantity originalQty,
            Instant createdAt) {
        this.id = requireNonNull(id, "orderId");
        this.userId = requireNonNull(userId, "userId");
        this.side = requireNonNull(side, "side");
        this.price = requireNonNull(price, "price");
        this.originalQty = requireNonNull(originalQty, "originalQty");
        this.createdAt = requireNonNull(createdAt, "createdAt");

        if (originalQty.isZero()) {
            throw new DomainException("originalQty must be > 0");
        }

        this.remainingQty = Quantity.ofNonNegative(originalQty.value());
    }

    public static Order create(
            UserId userId,
            Side side,
            Price price,
            Quantity originalQty) {
        return new Order(
                OrderId.newId(),
                userId,
                side,
                price,
                originalQty,
                Instant.now());
    }

    public OrderId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public Side side() {
        return side;
    }

    public Price price() {
        return price;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Quantity originalQty() {
        return originalQty;
    }

    public Quantity remainingQty() {
        return remainingQty;
    }

    public boolean isFilled() {
        return remainingQty.isZero();
    }

    public void fill(Quantity executedQty) {
        if (executedQty == null) {
            throw new DomainException("executedQty must not be null");
        }
        if (executedQty.isZero()) {
            throw new DomainException("executedQty must be > 0");
        }
        if (executedQty.value() > remainingQty.value()) {
            throw new DomainException("executedQty cannot exceed remainingQty");
        }
        remainingQty = remainingQty.minus(executedQty);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Order that))
            return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static <T> T requireNonNull(T v, String field) {
        if (v == null)
            throw new DomainException(field + " must not be null");
        return v;
    }

}
