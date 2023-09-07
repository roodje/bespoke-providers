package com.yolt.providers.raiffeisenbank.ro.config;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.RO;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static com.yolt.providers.raiffeisenbank.ro.config.RaiffeisenBankRoBeanConfigV1.DISPLAY_NAME;
import static com.yolt.providers.raiffeisenbank.ro.config.RaiffeisenBankRoBeanConfigV1.IDENTIFIER;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class RaiffeisenBankRoDetailsProvider implements AisDetailsProvider {

    public static final String RAIFFEISEN_BANK_RO_SITE_ID = "654c5380-7ec7-480f-8963-f5bb43137b37";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(RAIFFEISEN_BANK_RO_SITE_ID, DISPLAY_NAME, IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD), of(RO))
                    .groupingBy(DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT, LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.REDIRECT, LoginRequirement.FORM))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
