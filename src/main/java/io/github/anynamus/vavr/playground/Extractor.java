package io.github.anynamus.vavr.playground;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.function.Function;

@FunctionalInterface
public interface Extractor<A> {
    Validation<Seq<String>, A> extract(List<String> row);

    default <B> Extractor<B> map(Function<? super A, ? extends B> mapper) {
        return row -> extract(row).map(mapper);
    }

    default <B> Extractor<B> flatMap(Function<? super A, Validation<Seq<String>, B>> mapper) {
        return row -> extract(row).flatMap(mapper);
    }
}