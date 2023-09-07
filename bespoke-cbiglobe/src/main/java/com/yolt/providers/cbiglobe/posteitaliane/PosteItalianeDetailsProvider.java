package com.yolt.providers.cbiglobe.posteitaliane;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.IT;
import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.CREDITOR_POSTAL_COUNTRY;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class PosteItalianeDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String POSTE_ITALIANE_SITE_ID = "3b9c876d-1d62-4a5b-9046-163757129238";
    private static final String PROVIDER_KEY = "POSTE_ITALIANE";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(POSTE_ITALIANE_SITE_ID, "Poste Italiane", PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(IT))
                    .groupingBy("Poste Italiane")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build()
    );

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(POSTE_ITALIANE_SITE_ID))
                    .providerKey(PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(CREDITOR_POSTAL_COUNTRY, new DynamicFieldOptions(true)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
