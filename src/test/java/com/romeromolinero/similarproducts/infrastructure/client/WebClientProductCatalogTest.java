package com.romeromolinero.similarproducts.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.romeromolinero.similarproducts.domain.ProductId;
import com.romeromolinero.similarproducts.domain.exception.CatalogTimeoutException;
import com.romeromolinero.similarproducts.domain.exception.CatalogUpstreamException;
import com.romeromolinero.similarproducts.domain.exception.ProductNotFoundException;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

class WebClientProductCatalogTest {

    private DisposableServer server;

    @BeforeEach
    void startServer() {
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes
                        .get("/product/1/similarids", (request, response) -> response
                                .header("Content-Type", "application/json")
                                .sendString(Mono.just("[2,3,4]")))
                        .get("/product/2", (request, response) -> response
                                .header("Content-Type", "application/json")
                                .sendString(Mono.just("""
                                        {"id":"2","name":"Dress","price":19.99,"availability":true}
                                        """)))
                        .get("/product/missing", (request, response) -> response.status(404).send())
                        .get("/product/broken", (request, response) -> response.status(500).send())
                        .get("/product/slow", (request, response) -> response
                                .header("Content-Type", "application/json")
                                .sendString(Mono.delay(Duration.ofMillis(250)).map(ignored -> """
                                        {"id":"slow","name":"Slow product","price":1,"availability":true}
                                        """))))
                .bindNow();
    }

    @AfterEach
    void stopServer() {
        server.disposeNow();
    }

    @Test
    void acceptsNumericIdsFromTheProvidedMock() {
        WebClientProductCatalog catalog = catalog();

        StepVerifier.create(catalog.findSimilarProductIds(new ProductId("1")))
                .assertNext(ids -> assertThat(ids).containsExactly("2", "3", "4"))
                .verifyComplete();
    }

    @Test
    void decodesAProductDetail() {
        StepVerifier.create(catalog().findProductById(new ProductId("2")))
                .assertNext(product -> {
                    assertThat(product.id()).isEqualTo("2");
                    assertThat(product.name()).isEqualTo("Dress");
                })
                .verifyComplete();
    }

    @Test
    void distinguishesNotFoundFromOtherUpstreamErrors() {
        WebClientProductCatalog catalog = catalog();

        StepVerifier.create(catalog.findProductById(new ProductId("missing")))
                .expectError(ProductNotFoundException.class)
                .verify();
        StepVerifier.create(catalog.findProductById(new ProductId("broken")))
                .expectError(CatalogUpstreamException.class)
                .verify();
    }

    @Test
    void boundsSlowDownstreamCallsWithAResponseTimeout() {
        StepVerifier.create(catalog(Duration.ofMillis(50))
                        .findProductById(new ProductId("slow")))
                .expectError(CatalogTimeoutException.class)
                .verify();
    }

    private WebClientProductCatalog catalog() {
        return catalog(Duration.ofSeconds(2));
    }

    private WebClientProductCatalog catalog(Duration responseTimeout) {
        ProductCatalogProperties properties = new ProductCatalogProperties(
                URI.create("http://localhost:" + server.port()),
                Duration.ofMillis(500),
                responseTimeout,
                20,
                100,
                Duration.ofSeconds(1));
        WebClient client = new ProductCatalogClientConfiguration()
                .productCatalogWebClient(properties);
        return new WebClientProductCatalog(client);
    }
}
