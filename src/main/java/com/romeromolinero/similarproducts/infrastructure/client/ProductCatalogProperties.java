package com.romeromolinero.similarproducts.infrastructure.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("clients.product-catalog")
public record ProductCatalogProperties(
        @NotNull URI baseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration responseTimeout,
        @Min(1) @Max(2_000) int maxConnections,
        @Min(1) @Max(10_000) int pendingAcquireMaxCount,
        @NotNull Duration pendingAcquireTimeout) {}
