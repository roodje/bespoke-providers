package com.yolt.providers.belfius.belfius;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.BelfiusSampleTypedAuthenticationMeans;
import com.yolt.providers.belfius.TestApp;
import com.yolt.providers.belfius.common.exception.ConsentResponseSizeException;
import com.yolt.providers.belfius.common.exception.LoginNotFoundException;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessMeans;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessToken;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import nl.ing.lovebird.providershared.form.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("belfius")
@AutoConfigureWireMock(stubs = "classpath:/mappings/belfius", httpsPort = 0, port = 0)
public class BelfiusDataProviderIntegrationTest {

    @Autowired
    private BelfiusDataProvider belfiusDataProvider;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("BelfiusObjectMapper")
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        authenticationMeans = BelfiusSampleTypedAuthenticationMeans.createTestAuthenticationMeans();
    }

    @Test
    public void shouldReturnFormStepForFirstGetLoginUrlCall() {
        //Given
        String loginState = UUID.randomUUID().toString();

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //When
        FormStep formStep = (FormStep) belfiusDataProvider.getLoginInfo(urlGetLogin);

        //Then
        assertThat(formStep.getForm().getFormComponents()).hasSize(2);

        TextField textField = (TextField) formStep.getForm().getFormComponents().get(0);
        assertThat(textField.getId()).isEqualTo("Iban");
        assertThat(textField.getDisplayName()).isEqualTo("IBAN");
        assertThat(textField.getLength()).isEqualTo(34);
        assertThat(textField.getMaxLength()).isEqualTo(34);

        List<SelectOptionValue> selectOptionValues = ((SelectField) formStep.getForm().getFormComponents().get(1)).getSelectOptionValues();
        assertThat(selectOptionValues).extracting("value", "displayName")
                .contains(tuple("fr", "FR"), tuple("nl", "NL"));
    }

    @Test
    public void shouldPerformActualGetLoginUrlLogicIfTriggeredAfterFormStep() {
        //Given
        String stateId = UUID.randomUUID().toString();
        String redirectUrl = "https://www.yolt.com/callback-acc";

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "BE80350454678790");
        filledInUserSiteFormValues.add("ConsentLanguage", "en");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setBaseClientRedirectUrl(redirectUrl)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setState(stateId)
                .build();

        //When
        RedirectStep redirectStep = (RedirectStep) belfiusDataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        //Then
        assertThat(redirectStep.getRedirectUrl())
                .isEqualTo("https://www.belfius.be/common/en/fw/generic/launcher.html" +
                        "?appkey=APP_KEY&apptoken=rdger6e5325drte5635trwe45rew5wr345we5" +
                        "&state="+stateId);
    }

    @Test
    public void shouldPerformActualGetLoginUrlLogicIfTriggeredAfterFormStepEvenIfIBANIsLowercase() {
        //Given
        String stateId = UUID.randomUUID().toString();
        String redirectUrl = "https://www.yolt.com/callback-acc";

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "be80350454678790");
        filledInUserSiteFormValues.add("ConsentLanguage", "en");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setBaseClientRedirectUrl(redirectUrl)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setState(stateId)
                .build();

        //When
        RedirectStep redirectStep = (RedirectStep) belfiusDataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        //Then
        assertThat(redirectStep.getRedirectUrl())
                .isEqualTo("https://www.belfius.be/common/en/fw/generic/launcher.html" +
                        "?appkey=APP_KEY&apptoken=rdger6e5325drte5635trwe45rew5wr345we5" +
                        "&state="+stateId);
    }

    @Test
    public void shouldThrowExceptionWhenConsentResponseWillNotReturnExactlyOneConsentUrl() {
        //Given
        String stateId = UUID.randomUUID().toString();
        String redirectUrl = "https://www.yolt.com/callback-acc";

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "BE80350454678123");
        filledInUserSiteFormValues.add("ConsentLanguage", "en");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setBaseClientRedirectUrl(redirectUrl)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setState(stateId)
                .build();

        // when
        Throwable thrown = catchThrowable(() -> belfiusDataProvider.createNewAccessMeans(urlCreateAccessMeansRequest));

        // then
        assertThat(thrown).isInstanceOf(LoginNotFoundException.class);
        assertThat(thrown).hasCauseInstanceOf(ConsentResponseSizeException.class);
        assertThat(thrown).hasMessage("Consent response should contain only one element, but has 2");
    }

    @Test
    public void shouldCorrectlyCreateAccessToken() {
        //Given
        UUID someUserId = UUID.randomUUID();
        String redirectUrl = "https://www.yolt.com/callback-acc?code=25sdfsdfsd5345";
        String baseUrl = "https://www.yolt.com/callback-acc";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(someUserId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setBaseClientRedirectUrl(baseUrl)
                .setProviderState("{\"language\":\"en\",\"codeVerifier\":\"sfs353DRT345D\"}")
                .setRestTemplateManager(restTemplateManager)
                .build();

        //When
        AccessMeansOrStepDTO result = belfiusDataProvider.createNewAccessMeans(urlCreateAccessMeans);

        //Then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(someUserId);
        assertThat(result.getAccessMeans().getAccessMeans()).contains("BELFIUS_ACCESS_TOKEN");
        assertThat(result.getAccessMeans().getAccessMeans()).contains("BELFIUS_REFRESH_TOKEN");
    }

    @Test
    public void shouldCorrectlyRefreshToken() throws JsonProcessingException, TokenInvalidException {
        //Given
        UUID someUserId = UUID.randomUUID();

        BelfiusGroupAccessToken token = new BelfiusGroupAccessToken();
        token.setAccessToken("BELFIUS_ACCESS_TOKEN");
        token.setRefreshToken("BELFIUS_REFRESH_TOKEN");
        token.setExpiresIn(3600L);
        token.setLogicalId("SOME_LOGICAL_ID");
        BelfiusGroupAccessMeans accessMeans = new BelfiusGroupAccessMeans(token, "en", "REDIRECT_URL");
        String serializedAccessMeans = objectMapper.writeValueAsString(accessMeans);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(someUserId, serializedAccessMeans, new Date(), new Date());

        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //When
        AccessMeansDTO result = belfiusDataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        //Then
        assertThat(result.getUserId()).isEqualTo(someUserId);
        assertThat(result.getAccessMeans()).contains("BELFIUS_ACCESS_TOKEN");
        assertThat(result.getAccessMeans()).contains("BELFIUS_REFRESH_TOKEN");
    }

    @Test
    public void shouldCorrectlyFetchData() throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        BelfiusGroupAccessToken token = new BelfiusGroupAccessToken();
        token.setAccessToken("BELFIUS_ACCESS_TOKEN");
        token.setRefreshToken("BELFIUS_REFRESH_TOKEN");
        token.setExpiresIn(3600L);
        token.setLogicalId("SOME_LOGICAL_ID");
        BelfiusGroupAccessMeans accessMeans = new BelfiusGroupAccessMeans(token, "en", "REDIRECT_URL");
        String serializedAccessMeans = objectMapper.writeValueAsString(accessMeans);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(UUID.randomUUID(), serializedAccessMeans, new Date(), new Date());

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = belfiusDataProvider.fetchData(urlFetchData);

        // then
        verifyAccount(dataProviderResponse.getAccounts());
        verifyTransactions(dataProviderResponse.getAccounts().get(0).getTransactions());
    }

    private void verifyAccount(List<ProviderAccountDTO> expectedAccounts) {
        ProviderAccountDTO expectedAccount = expectedAccounts.get(0);

        expectedAccount.validate();

        assertThat(expectedAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(expectedAccount.getCurrentBalance()).isEqualTo("123.12");
        assertThat(expectedAccount.getAvailableBalance()).isEqualTo("123.12");
        assertThat(expectedAccount.getAccountId()).isEqualTo("SOME_LOGICAL_ID");
        assertThat(expectedAccount.getName()).isEqualTo("ACCOUNT NAME");
        assertThat(expectedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(expectedAccount.getAccountNumber().getIdentification()).isEqualTo("BE80350454678790");
    }


    private void verifyTransactions(List<ProviderTransactionDTO> expectedTransactions) {
        assertThat(expectedTransactions).hasSize(3);

        ProviderTransactionDTO firstProviderTransactionDTO = expectedTransactions.get(0);
        firstProviderTransactionDTO.validate();
        assertThat(firstProviderTransactionDTO.getDateTime()).isEqualTo("2020-01-01T00:00Z");
        assertThat(firstProviderTransactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(firstProviderTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstProviderTransactionDTO.getAmount()).isEqualTo("123.12");
        assertThat(firstProviderTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        ExtendedTransactionDTO extendedTransactionForFirstTransaction = firstProviderTransactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionForFirstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransactionForFirstTransaction.getBookingDate()).isEqualTo("2020-01-01T00:00Z");
        assertThat(extendedTransactionForFirstTransaction.getValueDate()).isEqualTo("2020-01-01T00:00Z");
        assertThat(extendedTransactionForFirstTransaction.getTransactionAmount().getAmount()).isEqualTo("123.12");
        assertThat(extendedTransactionForFirstTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransactionForFirstTransaction.getCreditorName()).isEqualTo("TEST ACCOUNT");
        assertThat(extendedTransactionForFirstTransaction.getCreditorAccount().getValue()).isEqualTo("BE80350454678791");
        assertThat(extendedTransactionForFirstTransaction.getDebtorName()).isEqualTo("ACCOUNT NAME");
        assertThat(extendedTransactionForFirstTransaction.getDebtorAccount().getValue()).isEqualTo("BE80350454678790");
        assertThat(extendedTransactionForFirstTransaction.getRemittanceInformationUnstructured()).isEqualTo("REMITTANCE INFO");

        ProviderTransactionDTO secondProviderTransactionDTO = expectedTransactions.get(1);
        secondProviderTransactionDTO.validate();
        assertThat(secondProviderTransactionDTO.getDateTime()).isEqualTo("2020-01-02T00:00Z");
        assertThat(secondProviderTransactionDTO.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(secondProviderTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(secondProviderTransactionDTO.getAmount()).isEqualTo("100.12");
        assertThat(secondProviderTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        ExtendedTransactionDTO extendedTransactionForSecondTransaction = secondProviderTransactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionForSecondTransaction.getTransactionAmount().getAmount()).isEqualTo("-100.12");
        assertThat(extendedTransactionForSecondTransaction.getValueDate()).isNull();

        ProviderTransactionDTO thirdProviderTransactionDTO = expectedTransactions.get(2);
        thirdProviderTransactionDTO.validate();
        assertThat(thirdProviderTransactionDTO.getDateTime()).isEqualTo("2020-01-03T00:00Z");
        assertThat(thirdProviderTransactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(thirdProviderTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(thirdProviderTransactionDTO.getAmount()).isEqualTo("10000.12");
        assertThat(thirdProviderTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(thirdProviderTransactionDTO.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("10000.12");
    }
}
