package com.marcospaulo.orderbook.adapters.out.memory;

import com.marcospaulo.orderbook.application.ports.out.WalletRepository;
import com.marcospaulo.orderbook.domain.model.Wallet;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryWalletRepository implements WalletRepository {
    private final ConcurrentHashMap<UserId, Wallet> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Wallet> findByUserId(UserId userId) {
        return Optional.ofNullable(store.get(userId));
    }

    @Override
    public void save(Wallet wallet) {
        store.put(wallet.userId(), wallet);
    }
}
