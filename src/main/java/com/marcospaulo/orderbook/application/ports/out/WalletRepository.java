package com.marcospaulo.orderbook.application.ports.out;

import java.util.Optional;

import com.marcospaulo.orderbook.domain.model.Wallet;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

public interface WalletRepository {
    Optional<Wallet> findByUserId(UserId userId);

    void save(Wallet wallet);
}
