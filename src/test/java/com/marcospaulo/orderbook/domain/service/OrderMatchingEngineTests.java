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
    void noCrossingProducesNoTrades() {
        OrderBook book = new OrderBook();
        book.add(Order.create(uid(), Side.BUY, Price.of(new BigDecimal("9.00")), Quantity.ofPositive(10)));
        book.add(Order.create(uid(), Side.SELL, Price.of(new BigDecimal("10.00")), Quantity.ofPositive(10)));

        var engine = new OrderMatchingEngine(new RestingOrderPricingPolicy());
        var trades = engine.match(book);

        assertTrue(trades.isEmpty());
    }

    @Test
    void crossingProducesTradeWithMinQuantity() {
        OrderBook book = new OrderBook();
        Order bid = Order.create(uid(), Side.BUY, Price.of(new BigDecimal("10.00")), Quantity.ofPositive(10));
        Order ask = Order.create(uid(), Side.SELL, Price.of(new BigDecimal("9.00")), Quantity.ofPositive(4));

        book.add(bid);
        book.add(ask);

        var engine = new OrderMatchingEngine(new RestingOrderPricingPolicy());
        var trades = engine.match(book);

        assertEquals(1, trades.size());
        assertEquals(4, trades.get(0).quantity().value());

        assertEquals(6, bid.remainingQty().value());
        assertEquals(0, ask.remainingQty().value());
        assertTrue(book.bestBid().isPresent());
        assertTrue(book.bestAsk().isEmpty());
    }

    @Test
    void multipleTradesAcrossLevels() {
        OrderBook book = new OrderBook();

        book.add(Order.create(uid(), Side.BUY, Price.of(new BigDecimal("10.00")), Quantity.ofPositive(5)));
        book.add(Order.create(uid(), Side.BUY, Price.of(new BigDecimal("10.00")), Quantity.ofPositive(5)));

        book.add(Order.create(uid(), Side.SELL, Price.of(new BigDecimal("9.00")), Quantity.ofPositive(6)));

        var engine = new OrderMatchingEngine(new RestingOrderPricingPolicy());
        var trades = engine.match(book);

        assertEquals(2, trades.size());
        assertEquals(5, trades.get(0).quantity().value());
        assertEquals(1, trades.get(1).quantity().value());
    }

    private static UserId uid() {
        return UserId.of(UUID.randomUUID());
    }
}
