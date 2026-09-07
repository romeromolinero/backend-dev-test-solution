package com.romeromolinero.similarproducts.infrastructure.web;

import com.romeromolinero.similarproducts.domain.exception.CatalogTimeoutException;
import com.romeromolinero.similarproducts.domain.exception.CatalogUpstreamException;
import com.romeromolinero.similarproducts.domain.exception.InvalidProductIdException;
import com.romeromolinero.similarproducts.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class ApiExceptionHandler {

    @ExceptionHandler(InvalidProductIdException.class)
    ResponseEntity<ApiError> handleInvalidProductId(InvalidProductIdException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_ID", exception.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ProductNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(CatalogTimeoutException.class)
    ResponseEntity<ApiError> handleTimeout(CatalogTimeoutException exception) {
        return error(HttpStatus.GATEWAY_TIMEOUT, "CATALOG_TIMEOUT", exception.getMessage());
    }

    @ExceptionHandler(CatalogUpstreamException.class)
    ResponseEntity<ApiError> handleUpstream(CatalogUpstreamException exception) {
        return error(HttpStatus.BAD_GATEWAY, "CATALOG_UNAVAILABLE", exception.getMessage());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
