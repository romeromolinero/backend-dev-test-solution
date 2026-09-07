package com.romeromolinero.similarproducts.domain.exception;

public final class CatalogTimeoutException extends CatalogException {

    public CatalogTimeoutException(String resource, Throwable cause) {
        super("Product catalog timed out while requesting %s".formatted(resource), cause);
    }
}
