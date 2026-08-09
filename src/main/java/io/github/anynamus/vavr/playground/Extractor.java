package io.github.anynamus.vavr.playground;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

@FunctionalInterface
public interface Extractor<A> {
    Validation<Seq<String>, A> extract(List<String> row);
}