package com.yolt.providers.axabanque.keytrade;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.BE;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;

@Service
public class KeytradeDetailsProvider implements AisDetailsProvider {

    public static final String KEYTRADE_SITE_ID = "66bee776-2299-11ec-9621-0242ac130002";
    public static final String PROVIDER_KEY = "KEYTRADE_BANK";
    public static final String PROVIDER_DISPLAY_NAME = "Keytrade Bank";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(KEYTRADE_SITE_ID, PROVIDER_DISPLAY_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(BE))
                    .groupingBy(PROVIDER_DISPLAY_NAME)
                    .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                    .loginRequirements(of(REDIRECT))
                    .consentExpiryInDays(90)
                    .build());

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
