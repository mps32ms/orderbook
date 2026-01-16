package com.marcospaulo.orderbook.application.exception;

public final class BackpressureException extends ApplicationException {
    public BackpressureException(String message) {
        super(message);
    }
}
