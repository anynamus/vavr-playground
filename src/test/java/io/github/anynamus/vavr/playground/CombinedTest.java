package io.github.anynamus.vavr.playground;

import io.github.anynamus.vavr.playground.model.Address;
import io.github.anynamus.vavr.playground.model.Company;
import io.github.anynamus.vavr.playground.model.ExtractionError;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CombinedTest {

    @Test
    void shouldExtractWithCombinedWithoutError() {
        var name = Extractors.required(0).named("name");
        var style = Extractors.required(1).named("style");

        var number = Extractors.required(2).named("number");
        var street = Extractors.required(3).named("street");
        var zip = Extractors.required(4).named("zip");

        var address = Extractors.combine(
                number,
                street,
                zip,
                Address::new
        );

        var company = Extractors.combine(
                name,
                style,
                address,
                Company::new
        );

        var result = company.extract(List.of("Zip", "Inc", "1", "Blvd All", "12345"));

        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(new Company("Zip", "Inc", new Address("1", "Blvd All", "12345")));
    }

    @Test
    void shouldFailWithCombinedWithtError() {
        var name = Extractors.required(0).named("name");
        var style = Extractors.required(1).named("style");

        var number = Extractors.required(2).named("number");
        var street = Extractors.required(3).named("street");
        var zip = Extractors.required(4).named("zip");

        var address = Extractors.combine(
                number,
                street,
                zip,
                Address::new
        );

        var company = Extractors.combine(
                name,
                style,
                address,
                Company::new
        );

        var result = company.extract(List.of("My Company", "", "1", "Blvd All", ""));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getError()).isEqualTo(
                List.of(
                        new ExtractionError("style", "Value is required"),
                        new ExtractionError("zip", "Value is required")
                )
        );
    }
}