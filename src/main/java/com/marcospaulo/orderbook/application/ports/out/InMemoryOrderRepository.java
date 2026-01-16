package com.marcospaulo.orderbook.application.ports.out;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.vo.OrderId;

public final class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<OrderId, Order> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }
}
