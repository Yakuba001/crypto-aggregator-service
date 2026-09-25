package com.yakuba.client;

import com.yakuba.model.ExternalCurrencyResponseDto;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalCurrencyClient {

    private final RestClient client;
    private final Retry retry;

    public ExternalCurrencyClient(RestClient.Builder client,
                                  Retry retry,
                                  @Value("${integration.mock-crypto.url}") String baseUrl) {
        this.client = client.baseUrl(baseUrl).build();
        this.retry = retry;
        }

    public ExternalCurrencyResponseDto getCurrencyRate(String pair) {
        return retry.executeSupplier(() -> client.get()
                .uri("/v1/rates/{pair}", pair)
                .retrieve()
                .body(ExternalCurrencyResponseDto.class));
    }
}
