package com.marcospaulo.orderbook.domain.policy;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Price;

public class RestingOrderPricingPolicy implements TradePricingPolicy {
    @Override
    public Price determinePrice(Order buy, Order sell, RestingSide restingSide) {
        if (buy == null || sell == null)
            throw new DomainException("orders must not be null");
        if (restingSide == null)
            throw new DomainException("restingSide must not be null");

        return restingSide == RestingSide.BUY ? buy.price() : sell.price();
    }

}
