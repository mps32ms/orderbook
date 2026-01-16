package com.marcospaulo.orderbook.adapters.in.web;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcospaulo.orderbook.application.ports.out.WalletRepository;
import com.marcospaulo.orderbook.domain.model.Wallet;
import com.marcospaulo.orderbook.domain.model.vo.UserId;

import jakarta.validation.constraints.*;

@RestController
@RequestMapping("/funding")
public class FundingController {

    private final WalletRepository wallets;

    public FundingController(WalletRepository wallets) {
        this.wallets = wallets;
    }

    public record FundRequest(
            @NotBlank String userId,
            @NotNull @PositiveOrZero BigDecimal cash,
            @NotNull @PositiveOrZero BigDecimal vibranium) {
    }

    @PostMapping
    public ResponseEntity<Void> fund(@RequestBody FundRequest req) {
        UserId uid = UserId.of(UUID.fromString(req.userId()));

        Wallet w = wallets.findByUserId(uid)
                .orElseGet(() -> Wallet.create(uid, BigDecimal.ZERO, BigDecimal.ZERO));

        w.depositCash(req.cash());
        w.depositVibranium(req.vibranium());

        wallets.save(w);
        return ResponseEntity.noContent().build();
    }
}
