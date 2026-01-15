package com.marcospaulo.orderbook.domain.policy;

import com.marcospaulo.orderbook.domain.model.Order;
import com.marcospaulo.orderbook.domain.model.vo.Price;

public interface TradePricingPolicy {
    Price determinePrice(Order buy, Order sell, RestingSide restingSide);

}
