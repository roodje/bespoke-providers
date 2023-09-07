package com.yolt.providers.rabobank.config;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.DynamicFieldOptions;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.CREDITOR_POSTAL_COUNTRY;
import static com.yolt.providers.rabobank.config.RabobankAisDetailsProvider.RABOBANK_SITE_ID;
import static java.util.List.of;

@Service
public class RabobankPisDetailsProvider implements PisDetailsProvider {

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(RABOBANK_SITE_ID))
                    .providerKey("RABOBANK")
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(CREDITOR_POSTAL_COUNTRY, new DynamicFieldOptions(true)))
                    .requiresSubmitStep(false)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
