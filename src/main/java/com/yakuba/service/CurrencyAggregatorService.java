package com.yakuba.service;

import com.yakuba.client.ExternalCurrencyClient;
import com.yakuba.model.CurrencyRate;
import com.yakuba.model.ExternalCurrencyResponseDto;
import com.yakuba.repository.CurrencyRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyAggregatorService {

    private final ExternalCurrencyClient client;
    private final CurrencyRateRepository repository;

    public CurrencyAggregatorService(ExternalCurrencyClient client, CurrencyRateRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public CurrencyRate aggregateAndSaveRate(String pair) {
        ExternalCurrencyResponseDto response = client.getCurrencyRate(pair);
        if (!response.isAskGteBid()) {
            throw new IllegalArgumentException("Ask price must be greater than or equal to Bid price");
        }
        BigDecimal spread = response.ask().subtract(response.bid());
        CurrencyRate rate = new CurrencyRate(
                response.source(),
                response.target(),
                response.bid(),
                response.ask(),
                spread,
                response.timestamp());
        return repository.save(rate);
    }
}
