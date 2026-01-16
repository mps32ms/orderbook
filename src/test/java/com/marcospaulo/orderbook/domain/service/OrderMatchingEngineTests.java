package com.marcospaulo.orderbook.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.OrderBook;
import com.marcospaulo.orderbook.domain.model.Side;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;
import com.marcospaulo.orderbook.domain.policy.RestingOrderPricingPolicy;

public class OrderMatchingEngineTests {

    @Test
    void buyIncomingMatchesAsksUpToLimit() {
        OrderBook book = new OrderBook();
        book.add(Order.create(uid(), Side.SELL, Price.of(new BigDecimal("9.00")), Quantity.ofPositive(4)));

        Order incoming = Order.create(uid(), Side.BUY, Price.of(new BigDecimal("10.00")), Quantity.ofPositive(10));

        var engine = new OrderMatchingEngine(new RestingOrderPricingPolicy());
        var trades = engine.matchIncoming(incoming, book);

        assertEquals(1, trades.size());
        assertEquals(4, trades.get(0).quantity().value());
        assertEquals("9.00", trades.get(0).price().toString());

        assertEquals(6, incoming.remainingQty().value());
        assertTrue(book.bestAsk().isEmpty());
    }

    @Test
    void sellIncomingMatchesBidsDownToLimit() {
        OrderBook book = new OrderBook();
        book.add(Order.create(uid(), Side.BUY, Price.of(new BigDecimal("10.00")), Quantity.ofPositive(5)));

        Order incoming = Order.create(uid(), Side.SELL, Price.of(new BigDecimal("9.00")), Quantity.ofPositive(2));

        var engine = new OrderMatchingEngine(new RestingOrderPricingPolicy());
        var trades = engine.matchIncoming(incoming, book);

        assertEquals(1, trades.size());
        assertEquals(2, trades.get(0).quantity().value());
        assertEquals("10.00", trades.get(0).price().toString());

        assertEquals(0, incoming.remainingQty().value());
        assertTrue(incoming.isFilled());
    }

    private static UserId uid() {
        return UserId.of(UUID.randomUUID());
    }
}
