package com.marcospaulo.orderbook.adapters.in.web;

import java.math.BigDecimal;

import com.marcospaulo.orderbook.domain.model.Wallet;

public record WalletResponse(
        BigDecimal cashAvailable,
        BigDecimal cashReserved,
        BigDecimal vibraniumAvailable,
        BigDecimal vibraniumReserved) {
    public static WalletResponse from(Wallet w) {
        return new WalletResponse(
                w.cash().available(),
                w.cash().reserved(),
                w.vibranium().available(),
                w.vibranium().reserved());
    }
}
