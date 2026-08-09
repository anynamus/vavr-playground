package io.github.anynamus.vavr.playground;

import io.github.anynamus.vavr.playground.model.ExtractionError;
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


        var result = Extractors.required(0, "lastName").extract(row);
        assertThat(result).isEqualTo(
                Validation.valid("John")
        );
    }

    @Test
    void requiredExtractorShouldRejectMissingColumn() {
        var row = List.of("John", "Doe");

        var result = Extractors.required(2, "firstName").extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, String>invalid(
                        List.of(
                                new ExtractionError(
                                        "firstName",
                                        "Column is missing"
                                )
                        )
                )
        );
    }

    @Test
    void shouldRejectBlankValue() {
        var row = List.of("John", "");

        var result = Extractors.required(1, "lastName").extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, String>invalid(
                        List.of(
                                new ExtractionError(
                                        "lastName",
                                        "Value is required"
                                )
                        )
                )
        );
    }

    @Test
    void shouldExtractOptionalValue() {
        var row = List.of("John", "Doe");

        var result = Extractors.optional(1, "lastName").extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(Option.of("Doe"))
        );
    }

    @Test
    void shouldReturnNoneForBlankValue() {
        var row = List.of("John", "");

        var result = Extractors.optional(1, "lastName").extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(Option.none())
        );
    }

    @Test
    void optionalExtractorShouldRejectMissingColumn() {
        var row = List.of("John");

        var result = Extractors.optional(2, "lastName").extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, Option<String>>invalid(
                        List.of(
                                new ExtractionError(
                                        "lastName",
                                        "Column is missing"
                                )
                        )
                )
        );
    }

    @Test
    void shouldMapExtractedValue() {
        var row = List.of("  John  ");

        var result = Extractors.required(0, "firstName")
                .map(String::trim)
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid("John")
        );
    }

    @Test
    void shouldPreserveExtractionError() {
        var row = List.of("");

        var result = Extractors.required(0, "firstName")
                .map(String::trim)
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, String>invalid(
                        List.of(new ExtractionError(
                                "firstName",
                                "Value is required"
                        ))
                )
        );
    }

    @Test
    void shouldFlatMapExtractedValue() {
        var row = List.of("John");

        var result = Extractors.required(0, "firstName")
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

        var result = Extractors.required(0, "firstName")
                .flatMap(value ->
                        Validation.invalid(
                                List.of(new ExtractionError(
                                        "firstName",
                                        "Value is required"
                                ))
                        )
                )
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, String>invalid(
                        List.of(new ExtractionError(
                                "firstName",
                                "Value is required"
                        ))
                )
        );
    }

    @Test
    void shouldExtractInteger() {
        var row = List.of("42");

        Extractor<String> stringExtractor = Extractors.required(0, "age").map(String::trim);

        var result = Extractors.asInt(stringExtractor)
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(42)
        );
    }

    @Test
    void shouldRejectInvalidInteger() {
        var row = List.of("abc");

        Extractor<String> stringExtractor = Extractors.required(0, "age").map(String::trim);
        var result = Extractors.asInt(stringExtractor).extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, Integer>invalid(
                        List.of(new ExtractionError(
                                "",
                                "Invalid integer: abc"
                        ))
                )
        );
    }

    @Test
    void shouldTrimBeforeConvertingToInteger() {
        var row = List.of(" 42 ");

        Extractor<String> stringExtractor = Extractors.required(0, "age").map(String::trim);
        var result = Extractors.asInt(stringExtractor).extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(42)
        );
    }

    @Test
    void shouldCombineTwoExtractors() {
        var firstName = Extractors.required(0, "firstName");
        var lastName = Extractors.required(1, "lastName");

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
        var firstName = Extractors.required(0,"firstName");
        var lastName = Extractors.required(1, "lastName");

        var person = Extractors.combine(
                firstName,
                lastName,
                Person::new
        );

        var result = person.extract(
                List.of("", "Doe")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, Person>invalid(
                        List.of(new ExtractionError(
                                "firstName",
                                "Value is required"
                        ))
                )
        );
    }

    @Test
    void shouldAccumulateErrors() {
        var firstName = Extractors.required(0, "firstName");
        var lastName = Extractors.required(1, "lastName");

        var person = Extractors.combine(
                firstName,
                lastName,
                Person::new
        );

        var result = person.extract(
                List.of("", "")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, Person>invalid(
                        List.of(
                                new ExtractionError(
                                        "firstName",
                                        "Value is required"
                                ),
                                new ExtractionError(
                                        "lastName",
                                        "Value is required"
                                )
                        )
                )
        );
    }
}