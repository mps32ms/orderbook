package com.marcospaulo.orderbook.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public class WalletTests {
    @Test
    void reserveForBuyMovesCashFromAvailableToReserved() {
        Wallet w = Wallet.create(UserId.of(UUID.randomUUID()), new BigDecimal("1000.00"), BigDecimal.ZERO);

        w.reserveForBuy(Price.of(new BigDecimal("10.00")), Quantity.ofPositive(10));

        assertEquals(new BigDecimal("900.00"), w.cash().available());
        assertEquals(new BigDecimal("100.00"), w.cash().reserved());
    }

    @Test
    void reserveForSellMovesVibraniumFromAvailableToReserved() {
        Wallet w = Wallet.create(UserId.of(UUID.randomUUID()), BigDecimal.ZERO, new BigDecimal("50"));

        w.reserveForSell(Quantity.ofPositive(10));

        assertEquals(new BigDecimal("40.00"), w.vibranium().available());
        assertEquals(new BigDecimal("10.00"), w.vibranium().reserved());
    }

    @Test
    void applyTradeAsBuyerDebitsReservedCreditsVibraniumAndReleasesChange() {
        Wallet w = Wallet.create(UserId.of(UUID.randomUUID()), new BigDecimal("1000.00"), BigDecimal.ZERO);

        Price limit = Price.of(new BigDecimal("10.00"));
        Price trade = Price.of(new BigDecimal("9.00"));
        Quantity qty = Quantity.ofPositive(10);

        w.reserveForBuy(limit, qty);

        w.applyTradeAsBuyer(limit, trade, qty);

        assertEquals(new BigDecimal("910.00"), w.cash().available());
        assertEquals(new BigDecimal("0.00"), w.cash().reserved());

        assertEquals(new BigDecimal("10.00"), w.vibranium().available());
    }

    @Test
    void applyTradeAsSellerDebitsReservedCreditsCash() {
        Wallet w = Wallet.create(UserId.of(UUID.randomUUID()), BigDecimal.ZERO, new BigDecimal("50"));

        Quantity qty = Quantity.ofPositive(10);
        w.reserveForSell(qty);

        w.applyTradeAsSeller(Price.of(new BigDecimal("10.00")), qty);

        assertEquals(new BigDecimal("100.00"), w.cash().available());
        assertEquals(new BigDecimal("0.00"), w.vibranium().reserved());
        assertEquals(new BigDecimal("40.00"), w.vibranium().available());
    }

    @Test
    void cannotReserveMoreThanAvailable() {
        Wallet w = Wallet.create(UserId.of(UUID.randomUUID()), new BigDecimal("50.00"), BigDecimal.ZERO);

        assertThrows(DomainException.class,
                () -> w.reserveForBuy(Price.of(new BigDecimal("10.00")), Quantity.ofPositive(6)));
    }

    @Test
    void buyerCannotTradeAboveLimitPrice() {
        Wallet w = Wallet.create(UserId.of(UUID.randomUUID()), new BigDecimal("1000.00"), BigDecimal.ZERO);

        Price limit = Price.of(new BigDecimal("10.00"));
        Quantity qty = Quantity.ofPositive(1);
        w.reserveForBuy(limit, qty);

        assertThrows(DomainException.class, () -> w.applyTradeAsBuyer(limit, Price.of(new BigDecimal("11.00")), qty));
    }

}
