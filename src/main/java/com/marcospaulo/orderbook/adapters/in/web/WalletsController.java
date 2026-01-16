package com.marcospaulo.orderbook.adapters.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcospaulo.orderbook.application.ports.out.WalletRepository;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

@RestController
@RequestMapping("/wallets")
public class WalletsController {

    private final WalletRepository wallets;

    public WalletsController(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> get(@PathVariable String userId) {
        UserId uid = UserId.of(UUID.fromString(userId));

        return wallets.findByUserId(uid)
                .map(WalletResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
