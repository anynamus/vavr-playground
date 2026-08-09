package io.github.anynamus.vavr.playground;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

public final class Extractors {

    private Extractors() {
    }

    public static Extractor<String> required(int column) {
        return row -> {
            if (column >= row.size()) {
                return Validation.invalid(
                        List.of("Column " + column + " is missing")
                );
            }

            String value = row.get(column);

            if (value == null || value.isBlank()) {
                return Validation.invalid(
                        List.of("Column " + column + " is required")
                );
            }

            return Validation.valid(value);
        };
    }

    public static Extractor<Option<String>> optional(int column) {
        return row -> {
            if (column >= row.size()) {
                return Validation.invalid(
                        List.of("Column " + column + " is missing")
                );
            }

            String value = row.get(column);

            return Validation.valid(
                    Option.of(value).filter(v -> !v.isBlank())
            );
        };
    }

    public static Extractor<Integer> asInt(Extractor<String> extractor) {
        return extractor.flatMap(value ->
                Try.of(() -> Integer.parseInt(value))
                        .fold(
                                error -> Validation.invalid(
                                        List.of("Invalid integer: " + value)
                                ),
                                Validation::valid
                        )
        );
    }
}