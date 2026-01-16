package com.marcospaulo.orderbook.application.command;

import java.util.Objects;

import com.marcospaulo.orderbook.application.ports.out.OrderBookRepository;
import com.marcospaulo.orderbook.application.ports.out.TradeRepository;
import com.marcospaulo.orderbook.application.ports.out.WalletRepository;

public final class CommandContext {

    private final OrderBookRepository orderBookRepository;
    private final WalletRepository walletRepository;
    private final TradeRepository tradeRepository;

    public CommandContext(
            OrderBookRepository orderBookRepository,
            WalletRepository walletRepository,
            TradeRepository tradeRepository) {
        this.orderBookRepository = Objects.requireNonNull(orderBookRepository, "orderBookRepository");
        this.walletRepository = Objects.requireNonNull(walletRepository, "walletRepository");
        this.tradeRepository = Objects.requireNonNull(tradeRepository, "tradeRepository");
    }

    public OrderBookRepository orderBookRepository() {
        return orderBookRepository;
    }

    public WalletRepository walletRepository() {
        return walletRepository;
    }

    public TradeRepository tradeRepository() {
        return tradeRepository;
    }
}
