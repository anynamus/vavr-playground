package io.github.anynamus.vavr.playground;

import io.github.anynamus.vavr.playground.model.ExtractionError;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.function.Function;

@FunctionalInterface
public interface Extractor<A> {
    Validation<Seq<ExtractionError>, A> extract(List<String> row);

    default <B> Extractor<B> map(Function<? super A, ? extends B> mapper) {
        return row -> extract(row).map(mapper);
    }

    default <B> Extractor<B> flatMap(Function<? super A, Validation<Seq<ExtractionError>, B>> mapper) {
        return row -> extract(row).flatMap(mapper);
    }
}