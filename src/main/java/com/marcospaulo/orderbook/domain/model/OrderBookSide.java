package com.marcospaulo.orderbook.domain.model;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Price;

public final class OrderBookSide {

    private final NavigableMap<Price, Deque<Order>> levels;

    public OrderBookSide(Comparator<Price> comparator) {
        this.levels = new TreeMap<>(Objects.requireNonNull(comparator, "comparator"));
    }

    public void add(Order order) {
        if (order == null)
            throw new DomainException("order must not be null");
        levels.computeIfAbsent(order.price(), p -> new ArrayDeque<>()).addLast(order);
    }

    public boolean isEmpty() {
        cleanupEmptyLevels();
        return levels.isEmpty();
    }

    public Optional<Price> bestPrice() {
        cleanupEmptyLevels();
        if (levels.isEmpty())
            return Optional.empty();
        return Optional.of(levels.firstKey());
    }

    public Optional<Order> peekBestOrder() {
        cleanupEmptyLevels();
        if (levels.isEmpty())
            return Optional.empty();
        Deque<Order> q = levels.firstEntry().getValue();
        if (q.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(q.peekFirst());
    }

    public Optional<Order> pollBestOrder() {
        cleanupEmptyLevels();
        if (levels.isEmpty())
            return Optional.empty();
        var entry = levels.firstEntry();
        Deque<Order> q = entry.getValue();
        Order o = q.pollFirst();
        if (q.isEmpty()) {
            levels.remove(entry.getKey());
        }
        return Optional.ofNullable(o);
    }

    public void putBackAtFront(Order order) {
        if (order == null)
            throw new DomainException("order must not be null");
        levels.computeIfAbsent(order.price(), p -> new ArrayDeque<>()).addFirst(order);
    }

    private void cleanupEmptyLevels() {
        while (!levels.isEmpty()) {
            var first = levels.firstEntry();
            if (first.getValue() != null && !first.getValue().isEmpty())
                return;
            levels.remove(first.getKey());
        }
    }

}
