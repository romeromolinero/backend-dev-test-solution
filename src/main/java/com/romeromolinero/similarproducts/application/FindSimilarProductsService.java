package com.romeromolinero.similarproducts.application;

import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import com.romeromolinero.similarproducts.domain.exception.CatalogException;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.Exceptions;
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
                // Consulta los detalles en paralelo, pero conserva la relevancia del catálogo.
                // El límite también evita saturar el servicio simulado con una lista muy grande.
                .flatMapSequential(
                        productCatalog::findProductById,
                        MAX_CONCURRENT_DETAIL_REQUESTS,
                        1)
                .collectList()
                // Reactor puede agrupar varios fallos simultáneos en una excepción compuesta.
                // Recupera el error del catálogo para mantener estable la respuesta HTTP pública.
                .onErrorMap(FindSimilarProductsService::unwrapCatalogFailure);
    }

    private static Throwable unwrapCatalogFailure(Throwable error) {
        if (error instanceof CatalogException) {
            return error;
        }
        return Exceptions.unwrapMultiple(error).stream()
                .filter(CatalogException.class::isInstance)
                .findFirst()
                .orElse(error);
    }
}
