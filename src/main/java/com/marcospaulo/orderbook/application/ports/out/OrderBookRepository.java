package com.marcospaulo.orderbook.application.ports.out;

import com.marcospaulo.orderbook.domain.model.OrderBook;

public interface OrderBookRepository {
    OrderBook get();

    void save(OrderBook book);
}
