package com.romeromolinero.similarproducts.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class ServiceInfoControllerTest {

    private final WebTestClient client = WebTestClient
            .bindToController(new ServiceInfoController())
            .build();

    @Test
    void explainsHowToTryTheServiceFromTheRootUrl() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.service").isEqualTo("Similar Products API")
                .jsonPath("$.status").isEqualTo("ready")
                .jsonPath("$.tryEndpoint").isEqualTo("GET /product/1/similar")
                .jsonPath("$.healthEndpoint").isEqualTo("GET /actuator/health");
    }
}
