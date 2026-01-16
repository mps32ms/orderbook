package com.marcospaulo.orderbook.application.command;

import java.util.List;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.OrderBook;
import com.marcospaulo.orderbook.domain.model.Side;
import com.marcospaulo.orderbook.domain.model.Trade;
import com.marcospaulo.orderbook.domain.model.Wallet;
import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;
import com.marcospaulo.orderbook.domain.policy.RestingOrderPricingPolicy;
import com.marcospaulo.orderbook.domain.service.OrderMatchingEngine;

public final class PlaceOrderCommand implements Command<PlaceOrderResult> {

    private final UserId userId;
    private final Side side;
    private final Price price;
    private final Quantity quantity;

    public PlaceOrderCommand(UserId userId, Side side, Price price, Quantity quantity) {
        this.userId = userId;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public PlaceOrderResult execute(CommandContext ctx) {
        if (userId == null)
            throw new DomainException("userId must not be null");
        if (side == null)
            throw new DomainException("side must not be null");
        if (price == null)
            throw new DomainException("price must not be null");
        if (quantity == null || quantity.isZero())
            throw new DomainException("quantity must be > 0");

        Wallet wallet = ctx.walletRepository()
                .findByUserId(userId)
                .orElseThrow(() -> new DomainException("wallet not found for userId=" + userId));

        // 1) Reserva antes de entrar no livro
        if (side == Side.BUY) {
            wallet.reserveForBuy(price, Quantity.ofPositive(quantity.value()));
        } else {
            wallet.reserveForSell(Quantity.ofPositive(quantity.value()));
        }
        ctx.walletRepository().save(wallet);

        // 2) Cria ordem incoming
        Order incoming = Order.create(userId, side, price, Quantity.ofPositive(quantity.value()));
        ctx.orderRepository().save(incoming);

        // 3) Matching contra livro
        OrderBook book = ctx.orderBookRepository().get();
        OrderMatchingEngine matcher = new OrderMatchingEngine(new RestingOrderPricingPolicy());

        List<Trade> trades = matcher.matchIncoming(incoming, book);

        // 4) Settlement por trade (wallets buyer/seller via OrderRepository)
        for (Trade t : trades) {
            Order buyOrder = ctx.orderRepository().findById(t.buyOrderId()).orElseThrow();
            Order sellOrder = ctx.orderRepository().findById(t.sellOrderId()).orElseThrow();

            Wallet buyer = ctx.walletRepository().findByUserId(buyOrder.userId()).orElseThrow();
            Wallet seller = ctx.walletRepository().findByUserId(sellOrder.userId()).orElseThrow();

            buyer.applyTradeAsBuyer(buyOrder.price(), t.price(), t.quantity());
            seller.applyTradeAsSeller(t.price(), t.quantity());

            ctx.walletRepository().save(buyer);
            ctx.walletRepository().save(seller);

            ctx.tradeRepository().append(t);

            // garante persistência do estado das ordens (referência mutável)
            ctx.orderRepository().save(buyOrder);
            ctx.orderRepository().save(sellOrder);
        }

        // 5) Se sobrar qty, entra no livro
        if (!incoming.isFilled()) {
            book.add(incoming);
        }

        ctx.orderBookRepository().save(book);
        ctx.orderRepository().save(incoming);

        return new PlaceOrderResult(incoming.id().toString(), trades.size());
    }
}
