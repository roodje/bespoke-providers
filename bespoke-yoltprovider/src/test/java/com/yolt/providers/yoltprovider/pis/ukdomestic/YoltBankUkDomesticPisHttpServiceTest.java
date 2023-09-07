package com.yolt.providers.yoltprovider.pis.ukdomestic;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.yoltprovider.YoltProviderConfigurationProperties;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.YoltBankUkPaymentErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YoltBankUkDomesticPisHttpServiceTest {

    private YoltBankUkDomesticHttpServiceImpl httpService;

    @Mock
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private YoltProviderConfigurationProperties properties;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private YoltBankUkPaymentErrorHandler errorHandler;

    @BeforeEach
    void beforeEach() {
        when(properties.getBaseUrl()).thenReturn("something");
        when(externalRestTemplateBuilderFactory.rootUri(anyString())).thenReturn(externalRestTemplateBuilderFactory);
        when(externalRestTemplateBuilderFactory.build()).thenReturn(restTemplate);

        httpService = new YoltBankUkDomesticHttpServiceImpl(
                externalRestTemplateBuilderFactory,
                errorHandler,
                properties
        );
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForPostInitiateSinglePayment() {
        // given
        HttpEntity<OBWriteDomesticConsent1> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/consent/single",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.postInitiateSinglePayment(httpEntity);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForPostInitiateScheduledPayment() {
        // given
        HttpEntity<OBWriteDomesticScheduledConsent1> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/consent/scheduled",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.postInitiateScheduledPayment(httpEntity);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForPostInitiatePeriodicPayment() {
        // given
        HttpEntity<OBWriteDomesticStandingOrderConsent1> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/consent/periodic",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.postInitiatePeriodicPayment(httpEntity);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForPostSubmitSinglePayment() {
        // given
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/single",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.postSubmitSinglePayment(httpEntity);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForPostSubmitPeriodicPayment() {
        // given
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/periodic",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.postSubmitPeriodicPayment(httpEntity);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForPostSubmitScheduledPayment() {
        // given
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/scheduled",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.postSubmitScheduledPayment(httpEntity);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForGetSinglePaymentStatus() {
        // given
        HttpEntity<Void> httpEntity = mock(HttpEntity.class);
        String fakePaymentId = "fakePaymentId";
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/single/{paymentId}",
                HttpMethod.GET,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.getSinglePaymentStatus(httpEntity, fakePaymentId);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForGetScheduledPaymentStatus() {
        // given
        HttpEntity<Void> httpEntity = mock(HttpEntity.class);
        String fakePaymentId = "fakePaymentId";
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/scheduled/{paymentId}",
                HttpMethod.GET,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.getScheduledPaymentStatus(httpEntity, fakePaymentId);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeForGetPeriodicPaymentStatus() {
        // given
        HttpEntity<Void> httpEntity = mock(HttpEntity.class);
        String fakePaymentId = "fakePaymentId";
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(restTemplate.exchange("/pis/uk/domestic/periodic/{paymentId}",
                HttpMethod.GET,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpService.getPeriodicPaymentStatus(httpEntity, fakePaymentId);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }
}
