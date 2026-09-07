package com.romeromolinero.similarproducts.domain;

import com.romeromolinero.similarproducts.domain.exception.InvalidProductIdException;
import java.util.regex.Pattern;

public record ProductId(String value) {

    private static final int MAX_LENGTH = 128;
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[A-Za-z0-9._-]+");

    public ProductId {
        if (value == null
                || value.isBlank()
                || value.length() > MAX_LENGTH
                || !ALLOWED_CHARACTERS.matcher(value).matches()) {
            throw new InvalidProductIdException();
        }
    }
}
