package com.marcospaulo.orderbook.domain.service;

import java.util.ArrayList;
import java.util.List;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.OrderBook;
import com.marcospaulo.orderbook.domain.model.Side;
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
     * Matching determinístico: incoming (taker) tenta cruzar com o livro.
     * Se sobrar quantidade, o incoming (parcial) deve ser adicionado ao book pela
     * camada application.
     */
    public List<Trade> matchIncoming(Order incoming, OrderBook book) {
        if (incoming == null)
            throw new DomainException("incoming order must not be null");
        if (book == null)
            throw new DomainException("orderBook must not be null");

        List<Trade> trades = new ArrayList<>();

        if (incoming.side() == Side.BUY) {
            // cruza contra asks enquanto bestAsk <= incoming.price
            while (!incoming.isFilled()) {
                Order bestAsk = book.bestAsk().orElse(null);
                if (bestAsk == null)
                    break;
                if (bestAsk.price().compareTo(incoming.price()) > 0)
                    break;

                bestAsk = book.asks().pollBestOrder().orElseThrow();

                long executed = Math.min(incoming.remainingQty().value(), bestAsk.remainingQty().value());
                Quantity executedQty = Quantity.ofPositive(executed);

                var tradePrice = pricingPolicy.determinePrice(incoming, bestAsk, RestingSide.SELL);

                incoming.fill(executedQty);
                bestAsk.fill(executedQty);

                trades.add(Trade.create(incoming.id(), bestAsk.id(), tradePrice, executedQty));

                if (!bestAsk.isFilled()) {
                    book.asks().putBackAtFront(bestAsk);
                }
            }

            return trades;
        }

        // SELL: cruza contra bids enquanto bestBid >= incoming.price
        while (!incoming.isFilled()) {
            Order bestBid = book.bestBid().orElse(null);
            if (bestBid == null)
                break;
            if (bestBid.price().compareTo(incoming.price()) < 0)
                break;

            bestBid = book.bids().pollBestOrder().orElseThrow();

            long executed = Math.min(incoming.remainingQty().value(), bestBid.remainingQty().value());
            Quantity executedQty = Quantity.ofPositive(executed);

            var tradePrice = pricingPolicy.determinePrice(bestBid, incoming, RestingSide.BUY);

            incoming.fill(executedQty);
            bestBid.fill(executedQty);

            trades.add(Trade.create(bestBid.id(), incoming.id(), tradePrice, executedQty));

            if (!bestBid.isFilled()) {
                book.bids().putBackAtFront(bestBid);
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
