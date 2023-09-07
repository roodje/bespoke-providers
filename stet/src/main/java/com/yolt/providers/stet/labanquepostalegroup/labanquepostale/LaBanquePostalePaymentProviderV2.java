package com.yolt.providers.stet.labanquepostalegroup.labanquepostale;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericPaymentProvider;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.payment.PaymentService;

public class LaBanquePostalePaymentProviderV2 extends GenericPaymentProvider {

    public LaBanquePostalePaymentProviderV2(AuthenticationMeansSupplier authMeansSupplier,
                                            HttpClientFactory httpClientFactory,
                                            PaymentService paymentService,
                                            DefaultProperties properties,
                                            ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, paymentService, properties, consentValidityRules);
    }

    @Override
    public String getProviderIdentifier() {
        return "LA_BANQUE_POSTALE";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "La Banque Postale";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_2;
    }
}
