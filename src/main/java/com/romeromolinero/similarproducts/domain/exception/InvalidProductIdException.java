package com.romeromolinero.similarproducts.domain.exception;

public final class InvalidProductIdException extends RuntimeException {

    public InvalidProductIdException() {
        super("El identificador del producto solo puede contener letras, números, puntos, guiones bajos o guiones");
    }
}
