package com.yakuba.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.yakuba.model.CurrencyRate;
import com.yakuba.repository.CurrencyRateRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class CurrencyAggregatorServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static WireMockServer server;

    @Autowired
    CurrencyRateRepository repository;

    @Autowired
    CurrencyAggregatorService service;

    @BeforeAll
    static void setUp() {
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
    }

    @AfterEach
    void setUpEach() {
        repository.deleteAllInBatch();
        server.resetAll();
        server.resetScenarios();
    }

    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("integration.mock-crypto.url", () -> server.baseUrl());
    }

    @Test
    void aggregateAndSaveRate_shouldSaveInDbAndReturnCurrencyRate_whenExternalApiReturns200AfterRetry() {
        String pair = "BTC-USD";
        String expectedPath = "/v1/rates/" + pair;
        String scenarioName = "Retry Scenario";

        server.stubFor(get(expectedPath)
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("Attempt 2")
                .willReturn(serviceUnavailable()));
        server.stubFor(get(expectedPath)
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 2")
                .willSetStateTo("Attempt 3")
                .willReturn(serviceUnavailable()));
        server.stubFor(get(expectedPath)
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 3")
                .willReturn(okJson("""
                        {
                        "source": "USD",
                        "target": "BTC",
                        "bid": 50000,
                        "ask": 50500,
                        "timestamp": "2026-09-25T10:00:00Z"
                        }
                        """)));

        CurrencyRate currencyRate = service.aggregateAndSaveRate(pair);
        assertNotNull(currencyRate);
        BigDecimal expectedSpread = currencyRate.getAskPrice().subtract(currencyRate.getBidPrice());

        assertThat(currencyRate.getSpread()).isEqualTo(expectedSpread);
        server.verify(3, getRequestedFor(urlEqualTo(expectedPath)));
    }

    @Test
    void aggregateAndSaveRate_shouldThrowExceptionAndNotRetry_whenExternalApiReturns400() {
        String pair = "BTC-USD";
        String expectedPath = "/v1/rates/" + pair;

        server.stubFor(get(urlEqualTo(expectedPath))
                .willReturn(badRequest()));

        assertThrows(HttpClientErrorException.class, () -> service.aggregateAndSaveRate(pair));

        server.verify(1, getRequestedFor(urlEqualTo(expectedPath)));
    }
}
