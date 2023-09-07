package com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

public class DateTimeAndUUIDBasedInstructionIdentificationSupplier implements Supplier<String> {

    private final Clock clock;
    private final Supplier<UUID> uniqueNumberSupplier;
    private final String dateTimeFormat;
    private final int maxSizeInstructionIdentification;

    public DateTimeAndUUIDBasedInstructionIdentificationSupplier(Clock clock) {
        this(clock, UUID::randomUUID, "yyyyMMddHHmmssSSS", 30);
    }

    public DateTimeAndUUIDBasedInstructionIdentificationSupplier(Clock clock, Supplier<UUID> uniqueNumberSupplier, String dateTimeFormat, int maxSizeInstructionIdentification) {
        this.clock = clock;
        this.uniqueNumberSupplier = uniqueNumberSupplier;
        this.dateTimeFormat = dateTimeFormat;
        this.maxSizeInstructionIdentification = maxSizeInstructionIdentification;
    }

    @Override
    public String get() {
        var dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        var currentDateTime = LocalDateTime.now(clock).format(dateTimeFormatter);
        var instructionIdentification = currentDateTime + "-" + uniqueNumberSupplier.get().toString();
        var maxSize = Math.min(maxSizeInstructionIdentification, instructionIdentification.length());
        return instructionIdentification.substring(0, maxSize);
    }
}
