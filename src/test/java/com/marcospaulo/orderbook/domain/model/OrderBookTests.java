package com.marcospaulo.orderbook.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public class OrderBookTests {

    @Test
    void asksUseLowestPriceFirst() {
        OrderBook book = new OrderBook();

        Order a1 = Order.create(UserId.of(UUID.randomUUID()), Side.SELL, Price.of(new BigDecimal("11.00")),
                Quantity.ofPositive(1));
        Order a2 = Order.create(UserId.of(UUID.randomUUID()), Side.SELL, Price.of(new BigDecimal("10.00")),
                Quantity.ofPositive(1));

        book.add(a1);
        book.add(a2);

        assertEquals("10.00", book.asks().bestPrice().orElseThrow().toString());
        assertEquals(a2.id(), book.bestAsk().orElseThrow().id());
    }

    @Test
    void bidsUseHighestPriceFirst() {
        OrderBook book = new OrderBook();

        Order b1 = Order.create(UserId.of(UUID.randomUUID()), Side.BUY, Price.of(new BigDecimal("9.00")),
                Quantity.ofPositive(1));
        Order b2 = Order.create(UserId.of(UUID.randomUUID()), Side.BUY, Price.of(new BigDecimal("10.00")),
                Quantity.ofPositive(1));

        book.add(b1);
        book.add(b2);

        assertEquals("10.00", book.bids().bestPrice().orElseThrow().toString());
        assertEquals(b2.id(), book.bestBid().orElseThrow().id());
    }

    @Test
    void fifoIsPreservedWithinSamePriceLevel() {
        OrderBook book = new OrderBook();

        UserId u = UserId.of(UUID.randomUUID());
        Price p = Price.of(new BigDecimal("10.00"));

        Order o1 = Order.create(u, Side.BUY, p, Quantity.ofPositive(1));
        Order o2 = Order.create(u, Side.BUY, p, Quantity.ofPositive(1));
        Order o3 = Order.create(u, Side.BUY, p, Quantity.ofPositive(1));

        book.add(o1);
        book.add(o2);
        book.add(o3);

        assertEquals(o1.id(), book.bids().pollBestOrder().orElseThrow().id());
        assertEquals(o2.id(), book.bids().pollBestOrder().orElseThrow().id());
        assertEquals(o3.id(), book.bids().pollBestOrder().orElseThrow().id());
    }

}
