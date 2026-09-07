package com.romeromolinero.similarproducts.infrastructure.web;

import com.romeromolinero.similarproducts.application.FindSimilarProductsService;
import com.romeromolinero.similarproducts.application.ProductCatalog;
import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import com.romeromolinero.similarproducts.domain.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

class SimilarProductsControllerTest {

    private StubProductCatalog catalog;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        catalog = new StubProductCatalog();
        SimilarProductsController controller = new SimilarProductsController(
                new FindSimilarProductsService(catalog));
        client = WebTestClient.bindToController(controller)
                .controllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsTheAgreedProductShape() {
        catalog.similarIds = Mono.just(List.of("2"));
        catalog.product = Mono.just(new ProductDetail(
                "2", "Dress", new BigDecimal("19.99"), true));

        client.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .json("""
                        [{"id":"2","name":"Dress","price":19.99,"availability":true}]
                        """);
    }

    @Test
    void mapsAMissingDownstreamProductToNotFound() {
        catalog.similarIds = Mono.just(List.of("missing"));
        catalog.product = Mono.error(new ProductNotFoundException("missing"));

        client.get()
                .uri("/product/4/similar")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void rejectsUnsafePathIdentifiers() {
        client.get()
                .uri("/product/not%20safe/similar")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_PRODUCT_ID");
    }

    private static final class StubProductCatalog implements ProductCatalog {

        private Mono<List<String>> similarIds = Mono.just(List.of());
        private Mono<ProductDetail> product = Mono.empty();

        @Override
        public Mono<List<String>> findSimilarProductIds(ProductId productId) {
            return similarIds;
        }

        @Override
        public Mono<ProductDetail> findProductById(ProductId productId) {
            return product;
        }
    }
}
