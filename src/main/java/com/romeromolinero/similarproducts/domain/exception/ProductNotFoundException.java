package com.romeromolinero.similarproducts.domain.exception;

public final class ProductNotFoundException extends CatalogException {

    public ProductNotFoundException(String productId) {
        super("No se encontró el producto '%s'".formatted(productId));
    }
}
