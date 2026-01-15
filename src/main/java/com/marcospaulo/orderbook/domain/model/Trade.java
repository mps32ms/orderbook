package com.marcospaulo.orderbook.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.OrderId;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.TradeId;

public final class Trade {
    private final TradeId id;
    private final OrderId buyOrderId;
    private final OrderId sellOrderId;
    private final Price price;
    private final Quantity quantity;
    private final Instant executedAt;

    private Trade(
            TradeId id,
            OrderId buyOrderId,
            OrderId sellOrderId,
            Price price,
            Quantity quantity,
            Instant executedAt) {
        this.id = requireNonNull(id, "tradeId");
        this.buyOrderId = requireNonNull(buyOrderId, "buyOrderId");
        this.sellOrderId = requireNonNull(sellOrderId, "sellOrderId");
        this.price = requireNonNull(price, "price");
        this.quantity = requireNonNull(quantity, "quantity");
        this.executedAt = requireNonNull(executedAt, "executedAt");

        if (quantity.isZero()) {
            throw new DomainException("trade quantity must be > 0");
        }
    }

    public static Trade create(
            OrderId buyOrderId,
            OrderId sellOrderId,
            Price price,
            Quantity quantity) {
        return new Trade(
                TradeId.newId(),
                buyOrderId,
                sellOrderId,
                price,
                quantity,
                Instant.now());
    }

    public TradeId id() {
        return id;
    }

    public OrderId buyOrderId() {
        return buyOrderId;
    }

    public OrderId sellOrderId() {
        return sellOrderId;
    }

    public Price price() {
        return price;
    }

    public Quantity quantity() {
        return quantity;
    }

    public Instant executedAt() {
        return executedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Trade that))
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
