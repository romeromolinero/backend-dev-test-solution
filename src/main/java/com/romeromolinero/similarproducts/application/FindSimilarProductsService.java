package com.romeromolinero.similarproducts.application;

import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public final class FindSimilarProductsService {

    private static final int MAX_CONCURRENT_DETAIL_REQUESTS = 8;

    private final ProductCatalog productCatalog;

    public FindSimilarProductsService(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    public Mono<List<ProductDetail>> findSimilarProducts(ProductId productId) {
        return productCatalog
                .findSimilarProductIds(productId)
                .flatMapMany(Flux::fromIterable)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .map(ProductId::new)
                .flatMapSequential(
                        productCatalog::findProductById,
                        MAX_CONCURRENT_DETAIL_REQUESTS,
                        1)
                .collectList();
    }
}
