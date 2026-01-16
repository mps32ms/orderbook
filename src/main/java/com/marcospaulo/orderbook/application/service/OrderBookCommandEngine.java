package com.marcospaulo.orderbook.application.service;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.marcospaulo.orderbook.application.command.Command;
import com.marcospaulo.orderbook.application.command.CommandContext;
import com.marcospaulo.orderbook.application.exception.ApplicationException;
import com.marcospaulo.orderbook.application.exception.BackpressureException;

public final class OrderBookCommandEngine implements AutoCloseable {

    private final BlockingQueue<QueuedCommand<?>> queue;
    private final CommandContext context;

    private final Semaphore capacity;
    private final Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public OrderBookCommandEngine(CommandContext context, int capacity) {
        this.context = Objects.requireNonNull(context, "context");
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must be > 0");

        this.capacity = new Semaphore(capacity);
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.worker = new Thread(this::runLoop, "orderbook-single-writer");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    public <R> CompletableFuture<R> submit(Command<R> command) {
        Objects.requireNonNull(command, "command");

        CompletableFuture<R> future = new CompletableFuture<>();

        if (!capacity.tryAcquire()) {
            future.completeExceptionally(new BackpressureException("engine capacity exceeded"));
            return future;
        }

        QueuedCommand<R> item = new QueuedCommand<>(command, future);

        boolean accepted = queue.offer(item);
        if (!accepted) {
            capacity.release();
            future.completeExceptionally(new BackpressureException("command queue is full"));
        }

        return future;
    }

    private void runLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                QueuedCommand<?> item = queue.poll(200, TimeUnit.MILLISECONDS);
                if (item == null)
                    continue;

                executeOne(item);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private <R> void executeOne(QueuedCommand<R> item) {
        try {
            R result = item.command.execute(context);
            item.future.complete(result);
        } catch (Throwable t) {
            item.future.completeExceptionally(new ApplicationException("command execution failed", t));
        } finally {
            capacity.release();
        }
    }

    @Override
    public void close() {
        running.set(false);
        worker.interrupt();
        try {
            worker.join(2_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class QueuedCommand<R> {
        private final Command<R> command;
        private final CompletableFuture<R> future;

        private QueuedCommand(Command<R> command, CompletableFuture<R> future) {
            this.command = command;
            this.future = future;
        }
    }
}
