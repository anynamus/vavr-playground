package io.github.anynamus.vavr.playground;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractorsTest {

    @Test
    void shouldExtractRequiredValue() {
        var row = List.of("John", "Doe");

        var result = Extractors.required(0).extract(row);

        assertThat(result).isEqualTo(
                Validation.valid("John")
        );
    }

    @Test
    void requiredExtractorShouldRejectMissingColumn() {
        var row = List.of("John");

        var result = Extractors.required(2).extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, String>invalid(
                        List.of("Column 2 is missing")
                )
        );
    }

    @Test
    void shouldRejectBlankValue() {
        var row = List.of("John", "");

        var result = Extractors.required(1).extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, String>invalid(
                        List.of("Column 1 is required")
                )
        );
    }

    @Test
    void shouldExtractOptionalValue() {
        var row = List.of("John", "Doe");

        var result = Extractors.optional(1).extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(Option.of("Doe"))
        );
    }

    @Test
    void shouldReturnNoneForBlankValue() {
        var row = List.of("John", "");

        var result = Extractors.optional(1).extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(Option.none())
        );
    }

    @Test
    void optinalExtractorShouldRejectMissingColumn() {
        var row = List.of("John");

        var result = Extractors.optional(2).extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, Option<String>>invalid(
                        List.of("Column 2 is missing")
                )
        );
    }
}