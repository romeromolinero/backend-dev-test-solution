package com.romeromolinero.similarproducts.domain.exception;

public final class InvalidProductIdException extends RuntimeException {

    public InvalidProductIdException() {
        super("Product id must contain only letters, numbers, dots, underscores, or hyphens");
    }
}
