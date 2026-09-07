package com.romeromolinero.similarproducts.infrastructure.web;

import com.romeromolinero.similarproducts.application.FindSimilarProductsService;
import com.romeromolinero.similarproducts.domain.ProductDetail;
import com.romeromolinero.similarproducts.domain.ProductId;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
public final class SimilarProductsController {

    private final FindSimilarProductsService service;

    public SimilarProductsController(FindSimilarProductsService service) {
        this.service = service;
    }

    @GetMapping("/{productId}/similar")
    public Mono<List<ProductDetail>> findSimilarProducts(@PathVariable String productId) {
        return service.findSimilarProducts(new ProductId(productId));
    }
}
