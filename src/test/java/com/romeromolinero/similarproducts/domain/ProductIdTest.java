package com.romeromolinero.similarproducts.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.romeromolinero.similarproducts.domain.exception.InvalidProductIdException;
import org.junit.jupiter.api.Test;

class ProductIdTest {

    @Test
    void acceptsSafeProductIdentifiers() {
        new ProductId("abc-123_v2.0");
    }

    @Test
    void rejectsUnsafeProductIdentifiers() {
        assertThatThrownBy(() -> new ProductId("../../products"))
                .isInstanceOf(InvalidProductIdException.class);
    }
}
