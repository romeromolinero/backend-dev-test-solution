package com.romeromolinero.similarproducts.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ProductDetail(String id, String name, BigDecimal price, boolean availability) {

    public ProductDetail {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del producto no puede estar vacío");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        Objects.requireNonNull(price, "El precio del producto no puede ser nulo");
    }
}
