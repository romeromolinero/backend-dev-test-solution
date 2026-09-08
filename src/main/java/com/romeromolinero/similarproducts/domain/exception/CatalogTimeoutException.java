package com.romeromolinero.similarproducts.domain.exception;

public final class CatalogTimeoutException extends CatalogException {

    public CatalogTimeoutException(String resource, Throwable cause) {
        super("El catálogo de productos agotó el tiempo de espera al solicitar %s".formatted(resource), cause);
    }
}
