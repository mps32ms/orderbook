package com.marcospaulo.orderbook.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.adapters.out.memory.InMemoryOrderBookRepository;
import com.marcospaulo.orderbook.adapters.out.memory.InMemoryTradeRepository;
import com.marcospaulo.orderbook.adapters.out.memory.InMemoryWalletRepository;
import com.marcospaulo.orderbook.application.command.Command;
import com.marcospaulo.orderbook.application.command.CommandContext;
import com.marcospaulo.orderbook.application.exception.BackpressureException;
import com.marcospaulo.orderbook.application.ports.out.InMemoryOrderRepository;

public class OrderBookCommandEngineTests {

    @Test
    void executesCommandsSeriallyAndCompletesAllFutures() throws Exception {
        CommandContext ctx = new CommandContext(
                new InMemoryOrderBookRepository(),
                new InMemoryWalletRepository(),
                new InMemoryTradeRepository(),
                new InMemoryOrderRepository());

        try (OrderBookCommandEngine engine = new OrderBookCommandEngine(ctx, 10_000)) {

            int n = 1_000;
            AtomicInteger counter = new AtomicInteger(0);

            ExecutorService pool = Executors.newFixedThreadPool(16);
            List<CompletableFuture<Integer>> futures = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    Command<Integer> cmd = c -> counter.incrementAndGet();
                    return engine.submit(cmd).join();
                }, pool));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

            assertEquals(n, counter.get());

            pool.shutdownNow();
        }
    }

    @Test
    void appliesBackpressureWhenQueueIsFull() {
        CommandContext ctx = new CommandContext(
                new InMemoryOrderBookRepository(),
                new InMemoryWalletRepository(),
                new InMemoryTradeRepository(),
                new InMemoryOrderRepository());

        try (OrderBookCommandEngine engine = new OrderBookCommandEngine(ctx, 1)) {
            CompletableFuture<Void> first = engine.submit(c -> {
                sleep(300);
                return null;
            });

            CompletableFuture<Void> second = engine.submit(c -> null);

            assertTrue(second.isCompletedExceptionally());

            try {
                second.join();
                fail("should have failed with backpressure");
            } catch (CompletionException ex) {
                assertTrue(ex.getCause() instanceof BackpressureException);
            }

            first.join();
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
