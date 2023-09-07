package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCashAccount51;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBStandingOrder6;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultStandingOrderMapper implements Function<OBStandingOrder6, StandingOrderDTO> {
    private final Function<String, Period> createPeriodFromFrequency;
    private final Function<String, BigDecimal> amountParser;
    private final SchemeMapper schemeMapper;
    private final Function<String, ZonedDateTime> zonedDateTimeMapper;

    @Override
    public StandingOrderDTO apply(OBStandingOrder6 standingOrder) {
        OBCashAccount51 creditorAccount = standingOrder.getCreditorAccount();
        ProviderAccountNumberDTO counterParty = null;
        if (creditorAccount != null) {
            String name = creditorAccount.getName();
            ProviderAccountNumberDTO.Scheme scheme = schemeMapper.mapToScheme(creditorAccount.getSchemeName());
            String accountNumber = creditorAccount.getIdentification();
            counterParty = new ProviderAccountNumberDTO(scheme, accountNumber);
            counterParty.setHolderName(name);
        }

        String nextPaymentAmount = standingOrder.getNextPaymentAmount().getAmount();
        Period frequency = createPeriodFromFrequency.apply(standingOrder.getFrequency());
        ZonedDateTime nextPaymentDateTime = offsetDateTimeToZonedDateTimeOrNull(standingOrder.getNextPaymentDateTime());
        ZonedDateTime finalPaymentDateTime = offsetDateTimeToZonedDateTimeOrNull(standingOrder.getFinalPaymentDateTime());
        return StandingOrderDTO.builder()
                .standingOrderId(standingOrder.getStandingOrderId())
                .description(standingOrder.getReference())
                .frequency(frequency)
                .nextPaymentDateTime(nextPaymentDateTime)
                .nextPaymentAmount(amountParser.apply(nextPaymentAmount))
                .finalPaymentDateTime(finalPaymentDateTime)
                .counterParty(counterParty)
                .build();
    }

    private ZonedDateTime offsetDateTimeToZonedDateTimeOrNull(final String dateTime) {
        return StringUtils.isEmpty(dateTime) ? null : zonedDateTimeMapper.apply(dateTime);
    }
}
