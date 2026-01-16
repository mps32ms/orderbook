package com.marcospaulo.orderbook.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.adapters.out.memory.InMemoryOrderBookRepository;
import com.marcospaulo.orderbook.adapters.out.memory.InMemoryTradeRepository;
import com.marcospaulo.orderbook.adapters.out.memory.InMemoryWalletRepository;
import com.marcospaulo.orderbook.application.command.CommandContext;
import com.marcospaulo.orderbook.application.command.PlaceOrderCommand;
import com.marcospaulo.orderbook.application.ports.out.InMemoryOrderRepository;
import com.marcospaulo.orderbook.domain.model.Side;
import com.marcospaulo.orderbook.domain.model.Wallet;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public class PlaceOrderIntegrationTests {

    @Test
    void placeSellThenBuyExecutesTradeAndSettlesWallets() {
        var orderBookRepo = new InMemoryOrderBookRepository();
        var walletRepo = new InMemoryWalletRepository();
        var tradeRepo = new InMemoryTradeRepository();
        var orderRepo = new InMemoryOrderRepository();

        var ctx = new CommandContext(orderBookRepo, walletRepo, tradeRepo, orderRepo);

        UserId sellerId = UserId.of(UUID.randomUUID());
        UserId buyerId = UserId.of(UUID.randomUUID());

        // Seller: 10 vib
        walletRepo.save(Wallet.create(sellerId, BigDecimal.ZERO, new BigDecimal("10")));
        // Buyer: 1000 cash
        walletRepo.save(Wallet.create(buyerId, new BigDecimal("1000.00"), BigDecimal.ZERO));

        try (OrderBookCommandEngine engine = new OrderBookCommandEngine(ctx, 100)) {

            // 1) seller coloca SELL 5 @ 10.00
            var sellRes = engine.submit(new PlaceOrderCommand(
                    sellerId,
                    Side.SELL,
                    Price.of(new BigDecimal("10.00")),
                    Quantity.ofPositive(5))).join();

            assertEquals(0, sellRes.tradesExecuted());

            // 2) buyer coloca BUY 3 @ 11.00 -> deve cruzar com resting SELL @ 10.00
            var buyRes = engine.submit(new PlaceOrderCommand(
                    buyerId,
                    Side.BUY,
                    Price.of(new BigDecimal("11.00")),
                    Quantity.ofPositive(3))).join();

            assertEquals(1, buyRes.tradesExecuted());

            var seller = walletRepo.findByUserId(sellerId).orElseThrow();
            var buyer = walletRepo.findByUserId(buyerId).orElseThrow();

            // Seller vendeu 3 vib a 10.00 => +30 cash, -3 vib reservado
            assertEquals(new BigDecimal("30.00"), seller.cash().available());

            // Seller: reservou 5 vib (available=5, reserved=5). Vendeu 3 vib:
            // available permanece 5, reserved cai para 2, cash sobe para 30.
            assertEquals(new BigDecimal("5.00"), seller.vibranium().available());
            assertEquals(new BigDecimal("2.00"), seller.vibranium().reserved());

            // Buyer comprou 3 vib a 10.00:
            // Reservou 33.00 (11*3), gastou 30.00, liberou 3.00, recebeu 3 vib
            assertEquals(new BigDecimal("970.00"), buyer.cash().available());
            assertEquals(new BigDecimal("0.00"), buyer.cash().reserved());
            assertEquals(new BigDecimal("3.00"), buyer.vibranium().available());
        }
    }

}
