package com.yakuba.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record ExternalCurrencyResponseDto(
        @NotBlank String source,
        @NotBlank String target,
        @NotNull @Positive BigDecimal bid,
        @NotNull @Positive BigDecimal ask,
        @NotNull Instant timestamp
) {
    @AssertTrue(message = "Ask price must be greater than or equal to Bid price")
    public boolean isAskGetBid() {
        if (bid == null || ask == null) {
            return true;
        }
        return ask.compareTo(bid) >= 0;
    }
}
