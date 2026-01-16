package com.marcospaulo.orderbook.application.ports.out;

import java.util.List;

import com.marcospaulo.orderbook.domain.model.Trade;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public interface TradeRepository {
    void append(Trade trade);

    /**
     * MVP: query simples por usuário. Depois pode ser otimizado/indexado.
     */
    List<Trade> findByUser(UserId userId);

    List<Trade> findAll();
}
