package com.romeromolinero.similarproducts.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ProductDetail(String id, String name, BigDecimal price, boolean availability) {

    public ProductDetail {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        Objects.requireNonNull(price, "Product price must not be null");
    }
}
