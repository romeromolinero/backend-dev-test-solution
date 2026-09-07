package com.romeromolinero.similarproducts.infrastructure.client;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProductCatalogProperties.class)
class ProductCatalogClientConfiguration {

    @Bean
    WebClient productCatalogWebClient(ProductCatalogProperties properties) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("product-catalog")
                .maxConnections(properties.maxConnections())
                .pendingAcquireMaxCount(properties.pendingAcquireMaxCount())
                .pendingAcquireTimeout(properties.pendingAcquireTimeout())
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .compress(true)
                .option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Math.toIntExact(properties.connectTimeout().toMillis()))
                .responseTimeout(properties.responseTimeout());

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
