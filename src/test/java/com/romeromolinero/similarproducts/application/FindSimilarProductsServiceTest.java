package com.romeromolinero.similarproducts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

class FindSimilarProductsServiceTest {

    @Test
    void resolvesDetailsConcurrentlyAndPreservesSimilarityOrder() {
        TestPublisher<ProductDetail> secondProduct = TestPublisher.create();
        TestPublisher<ProductDetail> thirdProduct = TestPublisher.create();
        ProductDetail dress = product("2", "Dress");
        ProductDetail blazer = product("3", "Blazer");

        StubProductCatalog catalog = new StubProductCatalog(List.of("2", "3", "2"));
        catalog.publish("2", secondProduct);
        catalog.publish("3", thirdProduct);

        StepVerifier.create(new FindSimilarProductsService(catalog)
                        .findSimilarProducts(new ProductId("1")))
                .then(() -> {
                    secondProduct.assertSubscribers();
                    thirdProduct.assertSubscribers();
                    thirdProduct.emit(blazer);
                    secondProduct.emit(dress);
                })
                .assertNext(products -> assertThat(products).containsExactly(dress, blazer))
                .verifyComplete();
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoSimilarIds() {
        StubProductCatalog catalog = new StubProductCatalog(List.of());

        StepVerifier.create(new FindSimilarProductsService(catalog)
                        .findSimilarProducts(new ProductId("1")))
                .expectNext(List.of())
                .verifyComplete();

        assertThat(catalog.detailRequestCount()).isZero();
    }

    private static ProductDetail product(String id, String name) {
        return new ProductDetail(id, name, new BigDecimal("19.99"), true);
    }

    private static final class StubProductCatalog implements ProductCatalog {

        private final List<String> similarIds;
        private final Map<String, Mono<ProductDetail>> products = new ConcurrentHashMap<>();
        private int detailRequestCount;

        private StubProductCatalog(List<String> similarIds) {
            this.similarIds = similarIds;
        }

        private void publish(String id, TestPublisher<ProductDetail> publisher) {
            products.put(id, publisher.mono());
        }

        private int detailRequestCount() {
            return detailRequestCount;
        }

        @Override
        public Mono<List<String>> findSimilarProductIds(ProductId productId) {
            return Mono.just(similarIds);
        }

        @Override
        public Mono<ProductDetail> findProductById(ProductId productId) {
            detailRequestCount += 1;
            return products.get(productId.value());
        }
    }
}
