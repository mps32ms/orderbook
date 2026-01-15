package com.marcospaulo.orderbook.domain.model;

import java.util.Comparator;
import java.util.Optional;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public final class OrderBook {

    private final OrderBookSide bids;
    private final OrderBookSide asks;

    public OrderBook() {
        this.bids = new OrderBookSide(Comparator.reverseOrder());
        this.asks = new OrderBookSide(Comparator.naturalOrder());
    }

    public void add(Order order) {
        if (order == null)
            throw new DomainException("order must not be null");
        if (order.side() == Side.BUY) {
            bids.add(order);
            return;
        }
        asks.add(order);
    }

    public OrderBookSide bids() {
        return bids;
    }

    public OrderBookSide asks() {
        return asks;
    }

    public Optional<Order> bestBid() {
        return bids.peekBestOrder();
    }

    public Optional<Order> bestAsk() {
        return asks.peekBestOrder();
    }
}
