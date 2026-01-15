package com.marcospaulo.orderbook.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public class OrderTradeTests {

    @Test
    void orderFillReducesRemainingAndCanReachZero() {
        Order o = Order.create(
                UserId.of(UUID.randomUUID()),
                Side.BUY,
                Price.of(new BigDecimal("10")),
                Quantity.ofPositive(10));

        assertEquals(10, o.remainingQty().value());
        assertFalse(o.isFilled());

        o.fill(Quantity.ofPositive(3));
        assertEquals(7, o.remainingQty().value());

        o.fill(Quantity.ofPositive(7));
        assertEquals(0, o.remainingQty().value());
        assertTrue(o.isFilled());
    }

    @Test
    void orderFillRejectsInvalidExecutedQty() {
        Order o = Order.create(
                UserId.of(UUID.randomUUID()),
                Side.SELL,
                Price.of(new BigDecimal("10")),
                Quantity.ofPositive(5));

        assertThrows(DomainException.class, () -> o.fill(Quantity.ofNonNegative(0)));
        assertThrows(DomainException.class, () -> o.fill(Quantity.ofPositive(6)));
    }

    @Test
    void tradeRejectsZeroQuantity() {
        Order buy = Order.create(
                UserId.of(UUID.randomUUID()),
                Side.BUY,
                Price.of(new BigDecimal("10")),
                Quantity.ofPositive(1));
        Order sell = Order.create(
                UserId.of(UUID.randomUUID()),
                Side.SELL,
                Price.of(new BigDecimal("10")),
                Quantity.ofPositive(1));

        assertThrows(DomainException.class,
                () -> Trade.create(buy.id(), sell.id(), buy.price(), Quantity.ofNonNegative(0)));
    }

}
