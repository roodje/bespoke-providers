package com.yolt.providers.yoltprovider.pis.ukdomestic;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public class InstructionIdentificationProvider {

    private final int maxSizeInstructionIdentification;
    private final DateTimeFormatter formatter;
    private final Clock clock;

    public String getInstructionIdentification() {
        String currentDateTime = LocalDateTime.now(clock).format(formatter);
        return currentDateTime + "-" + UUID.randomUUID().toString().substring(0, maxSizeInstructionIdentification - currentDateTime.length() - 1);
    }
}
