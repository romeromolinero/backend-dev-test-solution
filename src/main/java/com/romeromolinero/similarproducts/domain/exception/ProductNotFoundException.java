package com.romeromolinero.similarproducts.domain.exception;

public final class ProductNotFoundException extends CatalogException {

    public ProductNotFoundException(String productId) {
        super("Product '%s' was not found".formatted(productId));
    }
}
