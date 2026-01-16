package com.marcospaulo.orderbook.application.command;

public interface Command<R> {
    R execute(CommandContext ctx);
}
