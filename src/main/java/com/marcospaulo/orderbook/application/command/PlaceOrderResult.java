package com.marcospaulo.orderbook.application.command;

public record PlaceOrderResult(String orderId, int tradesExecuted) {
}
