CREATE TABLE currency_rates
(
    id BIGSERIAL PRIMARY KEY,
    source_currency VARCHAR(10),
    target_currency VARCHAR(10),
    bid_price NUMERIC(18, 8),
    ask_price NUMERIC(18, 8),
    spread NUMERIC(18, 8),
    fetched_at TIMESTAMPTZ
);

CREATE INDEX idx_currency_rates_pair_time ON currency_rates (source_currency, target_currency, fetched_at DESC);