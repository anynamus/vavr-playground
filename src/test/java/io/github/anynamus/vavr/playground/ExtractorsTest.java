package io.github.anynamus.vavr.playground;

import io.github.anynamus.vavr.playground.model.ExtractionError;
import io.github.anynamus.vavr.playground.model.Person;
import io.github.anynamus.vavr.playground.model.PersonProfile;
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


        var result = Extractors.required(0).named("lastName").extract(row);
        assertThat(result).isEqualTo(
                Validation.valid("John")
        );
    }

    @Test
    void requiredExtractorShouldRejectMissingColumn() {
        var row = List.of("John", "Doe");

        var result = Extractors.required(2).named("firstName").extract(row);

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

        var result = Extractors.required(1).named("lastName").extract(row);

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

        var result = Extractors.optional(1).named("lastName").extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(Option.of("Doe"))
        );
    }

    @Test
    void shouldReturnNoneForBlankValue() {
        var row = List.of("John", "");

        var result = Extractors.optional(1).named("lastName").extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(Option.none())
        );
    }

    @Test
    void optionalExtractorShouldRejectMissingColumn() {
        var row = List.of("John");

        var result = Extractors.optional(2)
                .named("lastName").extract(row);

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

        var result = Extractors.required(0)
                .named("firstName")
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
                .named("firstName")
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

        var result = Extractors.required(0)
                .named("firstName")
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
                .named("firstName")
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

        Extractor<String> stringExtractor = Extractors.required(0).map(String::trim);

        var result = Extractors.asInt(stringExtractor)
                .named("age")
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(42)
        );
    }

    @Test
    void shouldRejectInvalidInteger() {
        var row = List.of("abc");

        Extractor<String> stringExtractor = Extractors.required(0).map(String::trim);
        var result = Extractors.asInt(stringExtractor)
                .named("age")
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, Integer>invalid(
                        List.of(new ExtractionError(
                                "age",
                                "Invalid integer: abc"
                        ))
                )
        );
    }

    @Test
    void shouldTrimBeforeConvertingToInteger() {
        var row = List.of(" 42 ");

        Extractor<String> stringExtractor = Extractors.required(0).map(String::trim);
        var result = Extractors.asInt(stringExtractor)
                .named("age")
                .extract(row);

        assertThat(result).isEqualTo(
                Validation.valid(42)
        );
    }

    @Test
    void shouldCombineTwoExtractors() {
        var firstName = Extractors.required(0).named("firstName");
        var lastName = Extractors.required(1).named("lastName");

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
        var firstName = Extractors.required(0).named("firstName");
        var lastName = Extractors.required(1).named("lastName");

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
        var firstName = Extractors.required(0).named("firstName");
        var lastName = Extractors.required(1).named("lastName");

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

    @Test
    void shouldCombineThreeExtractors() {
        var firstName = Extractors.required(0).named("firstName");
        var lastName = Extractors.required(1).named("lastName");
        var age = Extractors.asInt(
                Extractors.required(2)
        )
                .named("age");

        var person = Extractors.combine(
                firstName,
                lastName,
                age,
                PersonProfile::new
        );

        var result = person.extract(
                List.of("John", "Doe", "42")
        );

        assertThat(result).isEqualTo(
                Validation.valid(
                        new PersonProfile("John", "Doe", 42)
                )
        );
    }

    @Test
    void shouldReturnErrorWhenOneExtractionFailsWithCombine3() {
        var firstName = Extractors.required(0).named("firstName");
        var lastName = Extractors.required(1).named("lastName");
        var age = Extractors.asInt(
                Extractors.required(2)
        ).named("age");

        var person = Extractors.combine(
                firstName,
                lastName,
                age,
                PersonProfile::new
        );

        var result = person.extract(
                List.of("John", "Doe", "abc")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, PersonProfile>invalid(
                        List.of(
                                new ExtractionError(
                                        "age",
                                        "Invalid integer: abc"
                                )
                        )
                )
        );
    }

    @Test
    void shouldAccumulateErrorsWithCombine3() {
        var firstName = Extractors.required(0).named("firstName");
        var lastName = Extractors.required(1).named("lastName");
        var age = Extractors.asInt(
                Extractors.required(2)
        )
                .named("age");

        var person = Extractors.combine(
                firstName,
                lastName,
                age,
                PersonProfile::new
        );

        var result = person.extract(
                List.of("", "", "abc")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, PersonProfile>invalid(
                        List.of(
                                new ExtractionError(
                                        "firstName",
                                        "Value is required"
                                ),
                                new ExtractionError(
                                        "lastName",
                                        "Value is required"
                                ),
                                new ExtractionError(
                                        "age",
                                        "Invalid integer: abc"
                                )
                        )
                )
        );
    }

    @Test
    void shouldNameExtractionError() {
        var extractor =
                Extractors.required(0)
                        .named("firstName");

        var result = extractor.extract(
                List.of("")
        );

        assertThat(result).isEqualTo(
                Validation.<Seq<ExtractionError>, String>invalid(
                        List.of(
                                new ExtractionError(
                                        "firstName",
                                        "Value is required"
                                )
                        )
                )
        );
    }

    @Test
    void shouldNotChangeValidValue() {
        var extractor =
                Extractors.required(0)
                        .named("firstName");

        var result = extractor.extract(
                List.of("John")
        );

        assertThat(result).isEqualTo(
                Validation.valid("John")
        );
    }
}