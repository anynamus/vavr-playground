package io.github.anynamus.vavr.playground;

import io.github.anynamus.vavr.playground.model.Person;
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

    @Test
    void shouldMapExtractedValue() {
        var row = List.of("  John  ");

        var result = Extractors.required(0)
                .map(String::trim)
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid("John")
        );
    }

    @Test
    void shouldPreserveExtractionError() {
        var row = List.of("");

        var result = Extractors.required(0)
                .map(String::trim)
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, String>invalid(
                        List.of("Column 0 is required")
                )
        );
    }

    @Test
    void shouldFlatMapExtractedValue() {
        var row = List.of("John");

        var result = Extractors.required(0)
                .flatMap(value ->
                        Validation.valid(value.toUpperCase())
                )
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid("JOHN")
        );
    }

    @Test
    void shouldReturnTransformationError() {
        var row = List.of("John");

        var result = Extractors.required(0)
                .flatMap(value ->
                        Validation.invalid(
                                List.of("Invalid value")
                        )
                )
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, String>invalid(
                        List.of("Invalid value")
                )
        );
    }

    @Test
    void shouldExtractInteger() {
        var row = List.of("42");

        Extractor<String> stringExtractor = Extractors.required(0).map(String::trim);

        var result = Extractors.asInt(stringExtractor)
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(42)
        );
    }

    @Test
    void shouldRejectInvalidInteger() {
        var row = List.of("abc");

        Extractor<String> stringExtractor = Extractors.required(0).map(String::trim);
        var result = Extractors.asInt(stringExtractor).extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, Integer>invalid(
                        List.of("Invalid integer: abc")
                )
        );
    }

    @Test
    void shouldTrimBeforeConvertingToInteger() {
        var row = List.of(" 42 ");

        Extractor<String> stringExtractor = Extractors.required(0).map(String::trim);
        var result = Extractors.asInt(stringExtractor).extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(42)
        );
    }

    @Test
    void shouldCombineTwoExtractors() {
        var firstName = Extractors.required(0);
        var lastName = Extractors.required(1);

        var person = Extractors.combine(
                firstName,
                lastName,
                Person::new
        );

        var result = person.extract(
                List.of("John", "Doe")
        );

        assertThat(result).isEqualTo(
                Validation.valid(
                        new Person("John", "Doe")
                )
        );
    }

    @Test
    void shouldReturnErrorWhenOneExtractionFails() {
        var firstName = Extractors.required(0);
        var lastName = Extractors.required(1);

        var person = Extractors.combine(
                firstName,
                lastName,
                Person::new
        );

        var result = person.extract(
                List.of("", "Doe")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, Person>invalid(
                        List.of("Column 0 is required")
                )
        );
    }

    @Test
    void shouldAccumulateErrors() {
        var firstName = Extractors.required(0);
        var lastName = Extractors.required(1);

        var person = Extractors.combine(
                firstName,
                lastName,
                Person::new
        );

        var result = person.extract(
                List.of("", "")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<String>, Person>invalid(
                        List.of(
                                "Column 0 is required",
                                "Column 1 is required"
                        )
                )
        );
    }
}