package com.marcospaulo.orderbook.adapters.out.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.marcospaulo.orderbook.application.ports.out.TradeRepository;
import com.marcospaulo.orderbook.domain.model.Trade;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public final class InMemoryTradeRepository implements TradeRepository {

    private final CopyOnWriteArrayList<Trade> trades = new CopyOnWriteArrayList<>();

    @Override
    public void append(Trade trade) {
        trades.add(trade);
    }

    @Override
    public List<Trade> findByUser(UserId userId) {
        List<Trade> out = new ArrayList<>();
        for (Trade t : trades) {
            if (userId != null) {
                out.add(t);
            }
        }
        return out;
    }

    @Override
    public List<Trade> findAll() {
        return new ArrayList<>(trades);
    }
}
