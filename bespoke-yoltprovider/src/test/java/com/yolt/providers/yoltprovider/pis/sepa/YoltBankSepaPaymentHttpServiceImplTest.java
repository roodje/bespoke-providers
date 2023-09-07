package com.yolt.providers.yoltprovider.pis.sepa;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.yoltprovider.YoltProviderConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaPaymentHttpServiceImplTest {

    private YoltBankSepaPaymentHttpServiceImpl httpService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private YoltProviderConfigurationProperties properties;

    @Captor
    private ArgumentCaptor<List<HttpMessageConverter<?>>> messageConverterCaptor;

    @BeforeEach
    public void beforeEach() {
        when(properties.getBaseUrl()).thenReturn("something");
        when(externalRestTemplateBuilderFactory.rootUri(anyString())).thenReturn(externalRestTemplateBuilderFactory);
        when(externalRestTemplateBuilderFactory.messageConverters(anyCollection())).thenReturn(externalRestTemplateBuilderFactory);
        when(externalRestTemplateBuilderFactory.build()).thenReturn(restTemplate);
        when(restTemplate.getMessageConverters()).thenReturn(Collections.emptyList());

        httpService = new YoltBankSepaPaymentHttpServiceImpl(externalRestTemplateBuilderFactory, properties);
    }

    @Test
    void shouldAddProperMessageConvertersOnInitialisation() {
        //given
        when(externalRestTemplateBuilderFactory.messageConverters(messageConverterCaptor.capture())).thenReturn(externalRestTemplateBuilderFactory);
        //when
        httpService = new YoltBankSepaPaymentHttpServiceImpl(externalRestTemplateBuilderFactory, properties);
        //then
        List<HttpMessageConverter<?>> messageConverters = messageConverterCaptor.getValue();
        assertThat(messageConverters).hasSize(1);
        assertThat(messageConverters.get(0)).isInstanceOf(ByteArrayHttpMessageConverter.class);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForPostInitiateSinglePaymentRequest() {
        // given
        HttpEntity<byte[]> httpEntity = Mockito.mock(HttpEntity.class);
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(restTemplate.exchange("/pis/sepa/single/initiate-payment", HttpMethod.POST, httpEntity, JsonNode.class))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpService.postInitiateSinglePaymentRequest(httpEntity);

        // then
        assertThat(result.getBody()).isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForPostInitiatePeriodicPaymentRequest() {
        // given
        HttpEntity<byte[]> httpEntity = Mockito.mock(HttpEntity.class);
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(restTemplate.exchange("/pis/sepa/periodic/initiate-payment",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpService.postInitiatePeriodicPaymentRequest(httpEntity);

        // then
        assertThat(result.getBody()).isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForPostSubmitSinglePaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(restTemplate.exchange("/pis/sepa/single/{paymentId}/submit",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpService.postSubmitSinglePaymentRequest(httpEntity, "fakePaymentId");

        // then
        assertThat(result.getBody()).isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForPostSubmitPeriodicPaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(restTemplate.exchange("/pis/sepa/periodic/{paymentId}/submit",
                HttpMethod.POST,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpService.postSubmitPeriodicPaymentRequest(httpEntity, "fakePaymentId");

        // then
        assertThat(result.getBody()).isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForGetSinglePaymentStatus() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(restTemplate.exchange("/pis/sepa/single/{paymentId}/status",
                HttpMethod.GET,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpService.getSingleStatus(httpEntity, "fakePaymentId");

        // then
        assertThat(result.getBody()).isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForGetPeriodicPaymentStatus() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(restTemplate.exchange("/pis/sepa/periodic/{paymentId}/status",
                HttpMethod.GET,
                httpEntity,
                JsonNode.class,
                "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpService.getPeriodicStatus(httpEntity, "fakePaymentId");

        // then
        assertThat(result.getBody()).isEqualTo(fakeResponseBody);
    }
}
