package com.marcospaulo.orderbook.adapters.out.memory;

import java.util.concurrent.atomic.AtomicReference;

import com.marcospaulo.orderbook.application.ports.out.OrderBookRepository;
import com.marcospaulo.orderbook.domain.model.OrderBook;

public final class InMemoryOrderBookRepository implements OrderBookRepository {

    private final AtomicReference<OrderBook> ref = new AtomicReference<>(new OrderBook());

    @Override
    public OrderBook get() {
        return ref.get();
    }

    @Override
    public void save(OrderBook book) {
        ref.set(book);
    }
}
