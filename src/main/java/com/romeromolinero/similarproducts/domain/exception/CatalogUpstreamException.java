package com.romeromolinero.similarproducts.domain.exception;

public final class CatalogUpstreamException extends CatalogException {

    public CatalogUpstreamException(String message) {
        super(message);
    }

    public CatalogUpstreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
