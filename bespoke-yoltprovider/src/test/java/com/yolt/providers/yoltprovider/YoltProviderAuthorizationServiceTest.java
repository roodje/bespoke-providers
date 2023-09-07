package com.yolt.providers.yoltprovider;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class YoltProviderAuthorizationServiceTest {
    private static final String YOLTBANK_EXCHANGE_URL = "/authorize/exchange";
    private static final String CODE = "any_string_wo_whitespaces";

    private YoltProviderAuthorizationService subject;

    private MockRestServiceServer server;
    private RestTemplate restTemplate;

    @BeforeEach
    public void beforeEach() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        subject = new YoltProviderAuthorizationService();
    }

    @Test
    public void shouldReturnAccessTokenForExchangeAuthorizationCodeForAccessToken() throws Exception {
        // given
        JSONObject requestObject = new JSONObject().put("code", CODE);
        JSONObject accessToken = new JSONObject().put("access_token", CODE);
        server.expect(requestTo(YOLTBANK_EXCHANGE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(requestObject.toString()))
                .andRespond(withSuccess(accessToken.toString(), MediaType.APPLICATION_JSON));

        // when
        final String actual = subject.exchangeAuthorizationCodeForAccessToken(restTemplate, CODE);

        // then
        assertThat(actual).isEqualTo(CODE);
    }
}
