package com.yakuba.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "currency_rates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "source_currency", nullable = false, updatable = false, length = 10)
    private String sourceCurrency;
    @Column(name = "target_currency", nullable = false, updatable = false, length = 10)
    private String targetCurrency;
    @Column(name = "bid_price", precision = 18, scale = 8)
    private BigDecimal bidPrice;
    @Column(name = "ask_price", precision = 18, scale = 8)
    private BigDecimal askPrice;
    @Column(name = "spread", precision = 18, scale = 8)
    private BigDecimal spread;
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyRate that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
