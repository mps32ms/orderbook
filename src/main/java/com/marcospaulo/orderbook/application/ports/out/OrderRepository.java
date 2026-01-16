package com.marcospaulo.orderbook.application.ports.out;

import java.util.Optional;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.vo.OrderId;

public interface OrderRepository {
    Optional<Order> findById(OrderId id);

    void save(Order order);
}
