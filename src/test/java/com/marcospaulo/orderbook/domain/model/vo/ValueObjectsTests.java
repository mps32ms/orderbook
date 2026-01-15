package com.marcospaulo.orderbook.domain.model.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.marcospaulo.orderbook.domain.model.exception.DomainException;

public class ValueObjectsTests {

    @Test
    void userIdRejectsBlank() {
        assertThrows(DomainException.class, () -> UserId.fromString(" "));
    }

    @Test
    void userIdAcceptsUuid() {
        UUID id = UUID.randomUUID();
        UserId userId = UserId.of(id);
        assertEquals(id, userId.value());
    }

    @Test
    void orderIdNewIdCreatesUuid() {
        OrderId id = OrderId.newId();
        assertNotNull(id.value());
    }

    @Test
    void priceNormalizesScaleAndMustBePositive() {
        Price p = Price.of(new BigDecimal("10"));
        assertEquals("10.00", p.value().toPlainString());

        assertThrows(DomainException.class, () -> Price.of(new BigDecimal("0")));
        assertThrows(DomainException.class, () -> Price.of(new BigDecimal("-1")));
    }

    @Test
    void quantityFactoriesWork() {
        assertThrows(DomainException.class, () -> Quantity.ofPositive(0));
        assertThrows(DomainException.class, () -> Quantity.ofPositive(-1));

        Quantity q0 = Quantity.ofNonNegative(0);
        assertTrue(q0.isZero());

        Quantity q10 = Quantity.ofPositive(10);
        assertEquals(10, q10.value());
    }

    @Test
    void quantityMinusCannotGoNegative() {
        Quantity q = Quantity.ofPositive(10);
        Quantity r = q.minus(Quantity.ofPositive(3));
        assertEquals(7, r.value());

        assertThrows(DomainException.class, () -> q.minus(Quantity.ofPositive(11)));
    }

}
