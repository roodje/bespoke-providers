package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalDirectDebitStatus1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadDirectDebit2DataDirectDebit;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultDirectDebitMapper implements Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> {
    private final ZoneId zoneId;
    private final Function<String, BigDecimal> amountParser;

    @Override
    public Optional<DirectDebitDTO> apply(OBReadDirectDebit2DataDirectDebit directDebit) {
        if (directDebit.getPreviousPaymentAmount() == null) {
            return Optional.empty();
        }
        String previousPaymentAmount = directDebit.getPreviousPaymentAmount().getAmount();
        ZonedDateTime previousPaymentDateTime = offsetDateTimeToZonedDateTimeOrNull(directDebit.getPreviousPaymentDateTime());
        return Optional.of(DirectDebitDTO.builder()
                .directDebitId(directDebit.getDirectDebitId())
                .description(directDebit.getName())
                .directDebitStatus(directDebit.getDirectDebitStatusCode() == OBExternalDirectDebitStatus1Code.ACTIVE)
                .previousPaymentAmount(amountParser.apply(previousPaymentAmount))
                .previousPaymentDateTime(previousPaymentDateTime)
                .build());
    }

    private ZonedDateTime offsetDateTimeToZonedDateTimeOrNull(final String input) {
        return input == null ? null : ZonedDateTime.parse(input).withZoneSameInstant(zoneId);
    }
}
