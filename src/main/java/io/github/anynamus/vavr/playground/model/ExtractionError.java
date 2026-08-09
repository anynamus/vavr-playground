package io.github.anynamus.vavr.playground.model;

public record ExtractionError(
        String field,
        String message
) {}