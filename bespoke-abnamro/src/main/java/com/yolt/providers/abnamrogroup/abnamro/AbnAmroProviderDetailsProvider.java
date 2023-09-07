package com.yolt.providers.abnamrogroup.abnamro;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.ProviderType;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.yolt.providers.common.providerdetail.dto.ConsentBehavior.CONSENT_PER_ACCOUNT;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.*;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class AbnAmroProviderDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String PROVIDER_NAME = "ABN AMRO";
    public static final String PROVIDER_KEY = "ABN_AMRO";
    private static final String SITE_ID = "7670247e-323e-4275-82f6-87f31119dbd3";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            AisSiteDetails.site(SITE_ID, PROVIDER_NAME, PROVIDER_KEY, ProviderType.DIRECT_CONNECTION, List.of(STATE), List.of(CURRENT_ACCOUNT), List.of(NL, BE, GB, FR, DE))
                    .groupingBy(PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, List.of(REDIRECT), PIS, List.of(REDIRECT)))
                    .loginRequirements(List.of(REDIRECT))
                    .consentBehavior(Set.of(CONSENT_PER_ACCOUNT))
                    .build()
    );

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(SITE_ID))
                    .providerKey(PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(List.of(REDIRECT))
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
