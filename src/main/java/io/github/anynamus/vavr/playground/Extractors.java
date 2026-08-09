package io.github.anynamus.vavr.playground;

import io.github.anynamus.vavr.playground.model.ExtractionError;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.util.function.Function;

public final class Extractors {

    private Extractors() {
    }

    public static Extractor<String> required(int column) {
        return row -> {
            if (column >= row.size()) {
                return Validation.invalid(
                        List.of(new ExtractionError(
                                "",
                                "Column is missing"
                        ))
                );
            }

            String value = row.get(column);

            if (value == null || value.isBlank()) {
                return Validation.invalid(
                        List.of(new ExtractionError(
                                "",
                                "Value is required"
                        ))
                );
            }

            return Validation.valid(value);
        };
    }

    public static Extractor<Option<String>> optional(int column) {
        return row -> {
            if (column >= row.size()) {
                return Validation.invalid(
                        List.of(new ExtractionError(
                                "",
                                "Column is missing"
                        ))
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
                                        List.of(new ExtractionError(
                                                "",
                                                "Invalid integer: " + value
                                        ))
                                ),
                                Validation::valid
                        )
        );
    }

    public static <A, B, R> Extractor<R> combine(
            Extractor<A> first,
            Extractor<B> second,
            Function2<A, B, R> constructor) {

        return row ->
                Validation.combine(
                                first.extract(row),
                                second.extract(row)
                        )
                        .ap(constructor)
                        .mapError(errors ->
                                errors.flatMap(Function.identity())
                        );
    }

    public static <A, B, C, R> Extractor<R> combine(
            Extractor<A> first,
            Extractor<B> second,
            Extractor<C> third,
            Function3<A, B, C, R> constructor) {

        return row ->
                Validation.combine(
                                first.extract(row),
                                second.extract(row),
                                third.extract(row)
                        )
                        .ap(constructor)
                        .mapError(errors ->
                                errors.flatMap(Function.identity())
                        );
    }
}