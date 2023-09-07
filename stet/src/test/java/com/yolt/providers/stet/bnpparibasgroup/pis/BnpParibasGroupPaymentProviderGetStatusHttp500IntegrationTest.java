package com.yolt.providers.stet.bnpparibasgroup.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BnpParibasGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/bnpparibasgroup/pis/status-500", httpsPort = 0, port = 0)
public class BnpParibasGroupPaymentProviderGetStatusHttp500IntegrationTest {

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signerMock;

    @Autowired
    @Qualifier("BnpParibasPaymentProviderV2")
    private GenericPaymentProviderV3 bnpParibasPaymentProviderV2;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private static final String PAYMENT_ID = "PAYMENT_REQUEST_RESOURCE_ID";
    private static final String SERIALIZED_PAYMENT_ID = "{\"paymentId\":\"" + PAYMENT_ID + "\"}";

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        authenticationMeans = new BnpParibasGroupSampleAuthenticationMeans().getBnpSampleAuthenticationMeans();
    }

    Stream<SepaPaymentProvider> getBnpParibasGroupPaymentProviders() {
        return Stream.of(bnpParibasPaymentProviderV2);
    }

    @ParameterizedTest
    @MethodSource("getBnpParibasGroupPaymentProviders")
    public void shouldGetPaymentStatus(SepaPaymentProvider provider) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(null)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("")
                .build();

        // when
        SepaPaymentStatusResponseDTO status = provider.getStatus(getStatusRequest);

        // then
        assertThat(status.getProviderState()).isNotEmpty();
        assertThat(status.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RJCT");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }
}
