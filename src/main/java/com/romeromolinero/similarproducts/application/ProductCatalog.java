package com.romeromolinero.similarproducts.application;

import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ProductCatalog {

    Mono<List<String>> findSimilarProductIds(ProductId productId);

    Mono<ProductDetail> findProductById(ProductId productId);
}
