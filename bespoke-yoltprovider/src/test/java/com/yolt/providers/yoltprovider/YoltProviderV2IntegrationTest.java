package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.TextField;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yolt.providers.yoltprovider.TestObjects.createAuthenticationMeans;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/ais", files = "classpath:/stubs/ais", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestApp.class})
public class YoltProviderV2IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String AUTHORIZATION_CODE_200 = "5f9c4ab08cac7457e9111a30e4664920607ea2c115a1433d7be98e97e64244ca";
    private static final String AUTHORIZATION_CODE_403 = "5f9c4ab08cac7457e9111a30e4664920607ea2c115a1433d7be98e97e64244cb";
    private static final String SECRET_STATE = "secretState";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback-dev";
    private static final String ACCESS_TOKEN = "accessToken";

    private final RestTemplateManagerMock restTemplateManager = new RestTemplateManagerMock();
    private final Signer signer = mock(Signer.class);
    private final ObjectMapper objectMapper = new YoltBankBeanConfig().yoltBankObjectMapper();

    @Autowired
    @Qualifier("YoltProviderVersion2")
    private YoltProviderVersion2 yoltProvider;

    @Autowired
    private YoltProviderConfigurationProperties configurationProperties;

    @Test
    public void shouldReturnRedirectStepWithRedirectUrlForGetLoginInfoWhenOnly1RedirectStep() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(new URI(loginInfo.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", SECRET_STATE);
    }

    @Test
    public void shouldReturnNewAccessMeansWithTheSameAuthCodeForCreateNewAccessMeansWithCorrectRequestData() throws Exception {
        // given
        RedirectFirstScenario formFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_200)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(providerState)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        AccessMeansDTO newAccessMeans = yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getAccessMeans();

        // then
        assertThat(newAccessMeans.getAccessMeans()).isEqualTo(AUTHORIZATION_CODE_200);
    }

    @ParameterizedTest
    @MethodSource("provideSelectFormTestData")
    public void shouldReturnFormStepWithSingleSelectFieldComponentForGetLoginInfo(
            String psuIpAddress,
            List<Pair<String, String>> expectedOptions) throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress(psuIpAddress)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(SelectField.class);

        SelectField selectField = (SelectField) firstFormStep.getForm().getFormComponents().get(0);
        List<Pair<String, String>> actualOptions = selectField.getSelectOptionValues().stream().map(o -> Pair.of(o.getValue(), o.getDisplayName())).collect(Collectors.toList());
        assertThat(actualOptions).containsExactlyElementsOf(expectedOptions);
    }

    @ParameterizedTest
    @MethodSource("provideQueryParamsForSelectForm")
    public void shouldReturnRedirectStepWithSelectedOptionForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided(
            String optionKey,
            String optionValue,
            FormFactoryContainer formFunction) throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add(optionKey, optionValue);
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(formFunction.formFactory.apply(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter(optionKey, optionValue);
    }

    @Test
    public void shouldReturnRedirectStepWithSelectedRegionForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvidedAndProviderStateDoesNotContainForm() throws URISyntaxException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("region", "region-a");
        UUID secondStateId = UUID.randomUUID();
        String providerState = "{\"type\":\"FORM_FIRST\",\"redirectUrl\":\"https://www.yolt.com/callback-dev\",\"customerAuthorizationUrl\":\"http://yoltbank.io/authorize\",\"authorizationCode\":null,\"accessMeans\":null}";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("region", "region-a");
    }

    @Test
    public void shouldReturnFormStepWithIbanComponentForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.2")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(TextField.class);
    }

    @Test
    public void shouldReturnRedirectStepWithIbanForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "NL12IBAN3456789");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.iban(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("Iban", "NL12IBAN3456789");
    }

    @Test
    public void shouldReturnFormStepWithLanguageSelectFieldAndIbanComponentsForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.5")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(2);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(SelectField.class);

        SelectField selectField = (SelectField) firstFormStep.getForm().getFormComponents().get(0);
        List<Pair<String, String>> actualOptions = selectField.getSelectOptionValues().stream().map(o -> Pair.of(o.getValue(), o.getDisplayName())).collect(Collectors.toList());
        assertThat(actualOptions).containsExactlyElementsOf(List.of(
                Pair.of("nl", "nederlands"),
                Pair.of("fr", "française")
        ));
        assertThat(firstFormStep.getForm().getFormComponents().get(1)).isInstanceOf(TextField.class);
    }

    @Test
    public void shouldReturnRedirectStepWithIbanAndLanguageForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "NL12IBAN3456789");
        filledInUserSiteFormValues.add("ConsentLanguage", "nl");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.ibanAndLanguage(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("Iban", "NL12IBAN3456789");
    }

    @Test
    public void shouldReturnFormStepWithBranchAndAccountComponentsForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.6")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(2);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(TextField.class);
        TextField textField1 = (TextField) firstFormStep.getForm().getFormComponents().get(0);
        assertThat(textField1.getId()).isEqualTo("branch-number");
        assertThat(firstFormStep.getForm().getFormComponents().get(1)).isInstanceOf(TextField.class);
        TextField textField2 = (TextField) firstFormStep.getForm().getFormComponents().get(1);
        assertThat(textField2.getId()).isEqualTo("account-number");
    }

    @Test
    public void shouldReturnRedirectStepWithAccountForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("branch-number", "987");
        filledInUserSiteFormValues.add("account-number", "1234567");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.branchAndAccount(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("account-number", "1234567");
    }

    @Test
    public void shouldReturnFormStepWithIbanAndUsernameComponentsForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.11")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(2);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(TextField.class);
        TextField textField1 = (TextField) firstFormStep.getForm().getFormComponents().get(0);
        assertThat(textField1.getId()).isEqualTo("Iban");
        assertThat(firstFormStep.getForm().getFormComponents().get(1)).isInstanceOf(TextField.class);
        TextField textField2 = (TextField) firstFormStep.getForm().getFormComponents().get(1);
        assertThat(textField2.getId()).isEqualTo("username");
    }

    @Test
    public void shouldReturnFormStepWithUsernameComponentsForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.7")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(TextField.class);
        TextField textField1 = (TextField) firstFormStep.getForm().getFormComponents().get(0);
        assertThat(textField1.getId()).isEqualTo("username");
    }

    @Test
    public void shouldReturnRedirectStepWithUsernameForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("username", "user12345");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.username(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("username", "user12345");
    }

    @Test
    public void shouldReturnFormStepWithEmailComponentsForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.8")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(TextField.class);
        TextField textField1 = (TextField) firstFormStep.getForm().getFormComponents().get(0);
        assertThat(textField1.getId()).isEqualTo("email");
    }

    @Test
    public void shouldReturnRedirectStepWithEmailForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("email", "John.Doe@yolt.com");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.email(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("email", "John.Doe@yolt.com");
    }

    @Test
    public void shouldReturnFormStepWithLanguageComponentsForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.9")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(SelectField.class);
        SelectField selectField = (SelectField) firstFormStep.getForm().getFormComponents().get(0);
        assertThat(selectField.getId()).isEqualTo("ConsentLanguage");
    }

    @Test
    public void shouldReturnRedirectStepWithLanguageForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("ConsentLanguage", "nl");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.languageSelection(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("ConsentLanguage", "nl");
    }

    @Test
    public void shouldReturnFormStepWithLoginIDComponentForGetLoginInfo() throws Exception {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(SECRET_STATE)
                .setPsuIpAddress("127.0.1.10")
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        FormStep firstFormStep = (FormStep) yoltProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(firstFormStep.getProviderState()).isNotEmpty();
        assertThat(firstFormStep.getForm().getFormComponents()).hasSize(1);
        assertThat(firstFormStep.getForm().getFormComponents().get(0)).isInstanceOf(TextField.class);
        TextField textField1 = (TextField) firstFormStep.getForm().getFormComponents().get(0);
        assertThat(textField1.getId()).isEqualTo("LoginID");
    }

    @Test
    public void shouldReturnRedirectStepWithLoginIDForCreateNewAccessMeansWhenFilledInUserSiteFormValuesProvided() throws URISyntaxException, JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("LoginID", "bankusername");
        UUID secondStateId = UUID.randomUUID();
        String providerState = objectMapper.writeValueAsString(FormFirstScenario.loginId(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl()));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(providerState)
                .setState(secondStateId.toString())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(new URI(redirectStep.getRedirectUrl()))
                .hasHost("yoltbank.io")
                .hasPath("/authorize")
                .hasParameter("redirect_uri", REDIRECT_URL)
                .hasParameter("state", secondStateId.toString())
                .hasParameter("LoginID", "bankusername");
    }

    @Test
    public void shouldReturnFormStepWithFormComponentsForCreateNewAccessMeansWhenTriggeringDynamicFlow() throws Exception {
        // given
        FormFirstScenario formFirstScenario = FormFirstScenario.regionSelection(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setProviderState(providerState)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_200 + "&trigger-dynamic-flow=elaborate-form")
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        FormStep formStep = (FormStep) yoltProvider
                .createNewAccessMeans(urlCreateAccessMeans)
                .getStep();

        // then
        assertThat(formStep.getForm().getFormComponents()).hasSize(7);
    }

    @Test
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWhenAllRequiredStepsFulfilled() throws Exception {
        // given
        RedirectFirstScenario formFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        formFirstScenario.setAuthorizationCode(AUTHORIZATION_CODE_200);
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeansSubmitForm = new UrlCreateAccessMeansRequestBuilder()
                .setFilledInUserSiteFormValues(new FilledInUserSiteFormValues())
                .setProviderState(providerState)
                .setState(UUID.randomUUID().toString())
                .setUserId(USER_ID)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        AccessMeansDTO newAccessMeans = yoltProvider
                .createNewAccessMeans(urlCreateAccessMeansSubmitForm)
                .getAccessMeans();

        // then
        assertThat(newAccessMeans.getAccessMeans()).isEqualTo(AUTHORIZATION_CODE_200);
    }

    @Test
    public void shouldReturnDataForFetchDataWithCorrectRequestData() throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        DataProviderResponse dataProviderResponse = yoltProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        accounts.forEach(account -> assertThat(account.getBankSpecific()).containsEntry("experimental", "true"));
    }

    @Test
    public void shouldThrowHttpClientErrorForbiddenExceptionForCreateNewAccessMeansWhenForbiddenErrorInResponse() throws Exception {
        // given
        RedirectFirstScenario formFirstScenario = new RedirectFirstScenario(REDIRECT_URL, configurationProperties.getCustomerAuthorizationUrl());
        String providerState = objectMapper.writeValueAsString(formFirstScenario);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + AUTHORIZATION_CODE_403)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(providerState)
                .setAuthenticationMeans(createAuthenticationMeans())
                .build();

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> yoltProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    private AccessMeansDTO createAccessMeansDTO() {
        return new AccessMeansDTO(USER_ID, ACCESS_TOKEN, new Date(), new Date());
    }



    private static Stream<Arguments> provideSelectFormTestData() {
        return Stream.of(
                Arguments.of("127.0.1.1", List.of(
                        Pair.of("region-a", "Region A"),
                        Pair.of("region-b", "Region B"),
                        Pair.of("region-c", "Region C")
                )),
                Arguments.of("127.0.1.3", List.of(
                        Pair.of("branch-1", "Branch Berlin"),
                        Pair.of("branch-3", "Branch Frankfurt"),
                        Pair.of("branch-4", "Branch Hanover"),
                        Pair.of("branch-2", "Branch Köln"),
                        Pair.of("branch-5", "Branch München")
                )),
                Arguments.of("127.0.1.4", List.of(
                        Pair.of("barclays", "Barclays"),
                        Pair.of("barclaycard", "Barclaycard"),
                        Pair.of("business", "Business")
                ))
        );
    }

    @AllArgsConstructor
    private static class FormFactoryContainer {
        BiFunction<String, String, FormFirstScenario> formFactory;
    }

    private static Stream<Arguments> provideQueryParamsForSelectForm() {
        return Stream.of(
                Arguments.of("region", "region-a", new FormFactoryContainer(FormFirstScenario::regionSelection)),
                Arguments.of("bank", "branch-1", new FormFactoryContainer(FormFirstScenario::bankSelection)),
                Arguments.of("AccountType", "business", new FormFactoryContainer(FormFirstScenario::accountTypeSelection))
        );
    }
}
