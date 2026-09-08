package com.romeromolinero.similarproducts.infrastructure.client;

import com.romeromolinero.similarproducts.application.ProductCatalog;
import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import com.romeromolinero.similarproducts.domain.exception.CatalogException;
import com.romeromolinero.similarproducts.domain.exception.CatalogTimeoutException;
import com.romeromolinero.similarproducts.domain.exception.CatalogUpstreamException;
import com.romeromolinero.similarproducts.domain.exception.ProductNotFoundException;
import io.netty.handler.timeout.ReadTimeoutException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public final class WebClientProductCatalog implements ProductCatalog {

    private static final ParameterizedTypeReference<List<String>> STRING_LIST =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public WebClientProductCatalog(WebClient productCatalogWebClient) {
        this.webClient = productCatalogWebClient;
    }

    @Override
    public Mono<List<String>> findSimilarProductIds(ProductId productId) {
        return get(
                "/product/{productId}/similarids",
                productId,
                STRING_LIST,
                "productos similares para '%s'".formatted(productId.value()));
    }

    @Override
    public Mono<ProductDetail> findProductById(ProductId productId) {
        return get(
                "/product/{productId}",
                productId,
                new ParameterizedTypeReference<>() {},
                "el producto '%s'".formatted(productId.value()));
    }

    private <T> Mono<T> get(
            String path,
            ProductId productId,
            ParameterizedTypeReference<T> responseType,
            String resource) {
        return webClient
                .get()
                .uri(path, productId.value())
                .exchangeToMono(response -> decode(
                        response.statusCode(),
                        response.bodyToMono(responseType),
                        productId,
                        resource))
                .onErrorMap(
                        error -> !(error instanceof CatalogException),
                        error -> mapTransportFailure(resource, error));
    }

    private <T> Mono<T> decode(
            HttpStatusCode status,
            Mono<T> body,
            ProductId productId,
            String resource) {
        if (status.is2xxSuccessful()) {
            return body.switchIfEmpty(Mono.error(new CatalogUpstreamException(
                    "El catálogo de productos devolvió una respuesta vacía al solicitar %s"
                            .formatted(resource))));
        }
        if (status.value() == 404) {
            return Mono.error(new ProductNotFoundException(productId.value()));
        }
        return Mono.error(new CatalogUpstreamException(
                "El catálogo de productos devolvió HTTP %d al solicitar %s"
                        .formatted(status.value(), resource)));
    }

    private CatalogException mapTransportFailure(String resource, Throwable error) {
        if (hasTimeoutCause(error)) {
            return new CatalogTimeoutException(resource, error);
        }
        return new CatalogUpstreamException(
                "No se pudo contactar con el catálogo de productos al solicitar %s"
                        .formatted(resource), error);
    }

    private boolean hasTimeoutCause(Throwable error) {
        // WebClient encapsula los fallos de Netty de forma distinta según dónde expire la petición,
        // por eso se revisa toda la cadena de causas antes de decidir el estado HTTP público.
        Throwable current = error;
        while (current != null) {
            if (current instanceof TimeoutException || current instanceof ReadTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
