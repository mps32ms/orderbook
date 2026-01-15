package com.marcospaulo.orderbook.domain.service;

import java.util.ArrayList;
import java.util.List;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.OrderBook;
import com.marcospaulo.orderbook.domain.model.Trade;
import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.policy.RestingSide;
import com.marcospaulo.orderbook.domain.policy.TradePricingPolicy;

public final class OrderMatchingEngine {

    private final TradePricingPolicy pricingPolicy;

    public OrderMatchingEngine(TradePricingPolicy pricingPolicy) {
        this.pricingPolicy = requireNonNull(pricingPolicy, "pricingPolicy");
    }

    /**
     * Executa matching até não haver mais cruzamento (bestBid < bestAsk).
     * Retorna a lista de trades gerados.
     */
    public List<Trade> match(OrderBook book) {
        if (book == null)
            throw new DomainException("orderBook must not be null");

        List<Trade> trades = new ArrayList<>();

        while (true) {
            Order bestBid = book.bestBid().orElse(null);
            Order bestAsk = book.bestAsk().orElse(null);

            if (bestBid == null || bestAsk == null)
                break;

            if (bestBid.price().compareTo(bestAsk.price()) < 0)
                break;

            bestBid = book.bids().pollBestOrder().orElseThrow();
            bestAsk = book.asks().pollBestOrder().orElseThrow();

            long executed = Math.min(bestBid.remainingQty().value(), bestAsk.remainingQty().value());
            Quantity executedQty = Quantity.ofPositive(executed);

            var tradePrice = pricingPolicy.determinePrice(bestBid, bestAsk, RestingSide.SELL);

            bestBid.fill(executedQty);
            bestAsk.fill(executedQty);

            trades.add(Trade.create(bestBid.id(), bestAsk.id(), tradePrice, executedQty));

            if (!bestBid.isFilled()) {
                book.bids().putBackAtFront(bestBid);
            }

            if (!bestAsk.isFilled()) {
                book.asks().putBackAtFront(bestAsk);
            }
        }

        return trades;
    }

    private static <T> T requireNonNull(T v, String field) {
        if (v == null)
            throw new DomainException(field + " must not be null");
        return v;
    }
}
