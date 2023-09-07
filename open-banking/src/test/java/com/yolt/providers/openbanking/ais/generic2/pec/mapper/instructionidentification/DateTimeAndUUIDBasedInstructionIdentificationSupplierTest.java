package com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DateTimeAndUUIDBasedInstructionIdentificationSupplierTest {

    private DateTimeAndUUIDBasedInstructionIdentificationSupplier subject;

    private final Clock clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00Z"), ZoneId.of("UTC"));

    @Test
    void shouldReturnInstructionIdentificationBasedOnCurrentTimeAndSuppliedUUIDWhenCorrectDataAreProvided() {
        // given
        UUID uuid = UUID.fromString("0b270719-a040-4e61-80ce-650ed608b2d7");
        int maxSizeInstructionIdentification = 54;
        Supplier<UUID> uniqueNumberSupplier = mock(Supplier.class);
        subject = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock, uniqueNumberSupplier, "yyyyMMddHHmmssSSS", maxSizeInstructionIdentification);

        given(uniqueNumberSupplier.get())
                .willReturn(uuid);
        // when
        String result = subject.get();

        // then
        then(uniqueNumberSupplier)
                .should()
                .get();

        assertThat(result).isEqualTo("20210101000000000-0b270719-a040-4e61-80ce-650ed608b2d7");
    }

    @Test
    void shouldReturnInstructionIdentificationInFullLengthWhenMaxSizeExceedsOriginalInstructionIdentificationLength() {
        UUID uuid = UUID.fromString("0b270719-a040-4e61-80ce-650ed608b2d7");
        int maxSizeInstructionIdentification = 160;
        Supplier<UUID> uniqueNumberSupplier = mock(Supplier.class);
        subject = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock, uniqueNumberSupplier, "yyyyMMddHHmmssSSS", maxSizeInstructionIdentification);

        given(uniqueNumberSupplier.get())
                .willReturn(uuid);

        // when
        String result = subject.get();

        // then
        then(uniqueNumberSupplier)
                .should()
                .get();

        assertThat(result).isEqualTo("20210101000000000-0b270719-a040-4e61-80ce-650ed608b2d7");
    }

    @Test
    void shouldReturnInstructionIdentificationCutToSpecificLengthWhenMaxSizeDoesNotExceedsOriginalInstructionIdentificationLength() {
        UUID uuid = UUID.fromString("0b270719-a040-4e61-80ce-650ed608b2d7");
        int maxSizeInstructionIdentification = 20;
        Supplier<UUID> uniqueNumberSupplier = mock(Supplier.class);
        subject = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock, uniqueNumberSupplier, "yyyyMMddHHmmssSSS", maxSizeInstructionIdentification);

        given(uniqueNumberSupplier.get())
                .willReturn(uuid);

        // when
        String result = subject.get();

        // then
        then(uniqueNumberSupplier)
                .should()
                .get();

        assertThat(result).isEqualTo("20210101000000000-0b");
    }
}