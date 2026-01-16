package com.marcospaulo.orderbook.domain.model;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;
import com.marcospaulo.orderbook.domain.model.vo.Price;
import com.marcospaulo.orderbook.domain.model.vo.Quantity;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public final class Wallet {

    private final UserId userId;
    private final Map<Asset, Balance> balances;

    private Wallet(UserId userId, Map<Asset, Balance> balances) {
        this.userId = requireNonNull(userId, "userId");
        this.balances = requireNonNull(balances, "balances");
    }

    public static Wallet create(UserId userId, BigDecimal initialCash, BigDecimal initialVibranium) {
        EnumMap<Asset, Balance> map = new EnumMap<>(Asset.class);
        map.put(Asset.CASH, Balance.of(nz(initialCash), BigDecimal.ZERO));
        map.put(Asset.VIBRANIUM, Balance.of(nz(initialVibranium), BigDecimal.ZERO));
        return new Wallet(userId, map);
    }

    public UserId userId() {
        return userId;
    }

    public Balance cash() {
        return balances.get(Asset.CASH);
    }

    public Balance vibranium() {
        return balances.get(Asset.VIBRANIUM);
    }

    /**
     * Reserva para BUY: cash = limitPrice * qty
     */
    public void reserveForBuy(Price limitPrice, Quantity qty) {
        requireNonNull(limitPrice, "limitPrice");
        requireNonNull(qty, "qty");
        if (qty.isZero())
            throw new DomainException("qty must be > 0");
        BigDecimal amount = limitPrice.value().multiply(BigDecimal.valueOf(qty.value()));
        cash().reserve(amount);
    }

    /**
     * Reserva para SELL: vibranium qty
     */
    public void reserveForSell(Quantity qty) {
        requireNonNull(qty, "qty");
        if (qty.isZero())
            throw new DomainException("qty must be > 0");
        BigDecimal amount = BigDecimal.valueOf(qty.value());
        vibranium().reserve(amount);
    }

    /**
     * Aplica trade do lado BUY:
     * - debita do reservado: tradePrice * qty
     * - credita vibranium disponível: qty
     * - libera troco (se tradePrice < limitPrice): (limitPrice - tradePrice) * qty
     */
    public void applyTradeAsBuyer(Price limitPrice, Price tradePrice, Quantity qty) {
        requireNonNull(limitPrice, "limitPrice");
        requireNonNull(tradePrice, "tradePrice");
        requireNonNull(qty, "qty");
        if (qty.isZero())
            throw new DomainException("qty must be > 0");

        BigDecimal q = BigDecimal.valueOf(qty.value());
        BigDecimal spent = tradePrice.value().multiply(q);
        BigDecimal reservedAtLimit = limitPrice.value().multiply(q);

        cash().debitReserved(spent);

        BigDecimal change = reservedAtLimit.subtract(spent);
        if (change.signum() < 0) {
            throw new DomainException("tradePrice cannot exceed limitPrice for buyer");
        }
        if (change.signum() > 0) {
            cash().release(change);
        }

        vibranium().creditAvailable(q);
    }

    /**
     * Aplica trade do lado SELL:
     * - debita do reservado: qty
     * - credita cash disponível: tradePrice * qty
     */
    public void applyTradeAsSeller(Price tradePrice, Quantity qty) {
        requireNonNull(tradePrice, "tradePrice");
        requireNonNull(qty, "qty");
        if (qty.isZero())
            throw new DomainException("qty must be > 0");

        BigDecimal q = BigDecimal.valueOf(Objects.requireNonNull(qty).value());
        vibranium().debitReserved(q);

        BigDecimal earned = tradePrice.value().multiply(q);
        cash().creditAvailable(earned);
    }

    private static BigDecimal nz(BigDecimal v) {
        if (v == null)
            return BigDecimal.ZERO;
        if (v.signum() < 0)
            throw new DomainException("initial balance must be >= 0");
        return v;
    }

    public void depositCash(BigDecimal amount) {
        requireNonNull(amount, "amount");
        if (amount.signum() < 0)
            throw new DomainException("cash deposit must be >= 0");
        balances.get(Asset.CASH).creditAvailable(amount);
    }

    public void depositVibranium(BigDecimal amount) {
        requireNonNull(amount, "amount");
        if (amount.signum() < 0)
            throw new DomainException("vibranium deposit must be >= 0");
        balances.get(Asset.VIBRANIUM).creditAvailable(amount);
    }

    private static <T> T requireNonNull(T value, String name) {
        if (value == null)
            throw new DomainException(name + " must not be null");
        return value;
    }

}
