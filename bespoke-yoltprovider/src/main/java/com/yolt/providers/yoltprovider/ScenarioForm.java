package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.form.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegionSelectionForm.class, name = "REGION_SELECTION"),
        @JsonSubTypes.Type(value = IbanForm.class, name = "IBAN"),
        @JsonSubTypes.Type(value = BankSelectionForm.class, name = "BANK_SELECTION"),
        @JsonSubTypes.Type(value = AccountTypeSelectionForm.class, name = "ACCOUNT_TYPE_SELECTION"),
        @JsonSubTypes.Type(value = IbanAndLanguageForm.class, name = "IBAN_AND_LANGUAGE_SELECTION"),
        @JsonSubTypes.Type(value = IbanAndUsernameForm.class, name = "IBAN_AND_USERNAME_SELECTION"),
        @JsonSubTypes.Type(value = BranchAndAccountForm.class, name = "BRANCH_AND_ACCOUNT"),
        @JsonSubTypes.Type(value = UsernameForm.class, name = "USERNAME"),
        @JsonSubTypes.Type(value = EmailForm.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = LanguageSelectionForm.class, name = "LANGUAGE"),
        @JsonSubTypes.Type(value = LoginIdForm.class, name = "LOGIN_ID"),
        @JsonSubTypes.Type(value = UsernameAndPasswordForm.class, name = "USERNAME_AND_PASSWORD"),
        @JsonSubTypes.Type(value = SimpleChallengeForm.class, name = "SIMPLE_CHALLENGE"),
        @JsonSubTypes.Type(value = FlickerChallengeForm.class, name = "FLICKER_CHALLENGE"),
        @JsonSubTypes.Type(value = PhotoOTPChallengeForm.class, name = "PHOTO_OTP_CHALLENGE")
})
@RequiredArgsConstructor
@Getter
abstract class ScenarioForm {
    /**
     * @return The form to be displayed to the user.
     */
    public abstract Form buildForm();

    /**
     * @param formValues The values entered by the user.
     */
    public abstract boolean isValid(FilledInUserSiteFormValues formValues);

    /**
     * @param formValues The values entered by the user.
     * @return Key-value pair to appear in the query string, if present.
     */
    public abstract Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues);
}

// Base class for scenario forms with a select field
abstract class SelectionScenarioForm extends ScenarioForm {
    protected final String fieldId;
    protected final String fieldDisplayName;
    protected final List<Pair<String, String>> options;
    protected final List<String> optionKeys;

    SelectionScenarioForm(String fieldId, String fieldDisplayName, List<Pair<String, String>> options) {
        this.fieldId = fieldId;
        this.fieldDisplayName = fieldDisplayName;
        this.options = options;
        this.optionKeys = options.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Form buildForm() {
        SelectField selectField = new SelectField(fieldId, fieldDisplayName, 0, 0, false, true);
        options.forEach(lang -> selectField.addSelectOptionValue(new SelectOptionValue(lang.getKey(), lang.getValue())));
        return new Form(List.of(selectField), null, null);
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return optionKeys.contains(formValues.get(fieldId));
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(fieldId))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of(fieldId, value));
    }
}

// Caja Rural, Credit Agricole, Banque Populaire, Caisse d'Espargne require to select a region first
class RegionSelectionForm extends SelectionScenarioForm {
    private static final String FIELD_ID = "region";
    private static final String FIELD_DISPLAY_NAME = "Select the region";
    private static final List<Pair<String, String>> REGIONS = List.of(
            Pair.of("region-a", "Region A"),
            Pair.of("region-b", "Region B"),
            Pair.of("region-c", "Region C")
    );

    RegionSelectionForm() {
        super(FIELD_ID, FIELD_DISPLAY_NAME, REGIONS);
    }
}

// Spaarkassen requires to select the bank first
class BankSelectionForm extends SelectionScenarioForm {
    private static final String FIELD_ID = "bank";
    private static final String FIELD_DISPLAY_NAME = "Select the bank";
    private static final List<Pair<String, String>> BANK_BRANCHES = List.of(
            Pair.of("branch-1", "Branch Berlin"),
            Pair.of("branch-3", "Branch Frankfurt"),
            Pair.of("branch-4", "Branch Hanover"),
            Pair.of("branch-2", "Branch Köln"),
            Pair.of("branch-5", "Branch München")
    );

    BankSelectionForm() {
        super(FIELD_ID, FIELD_DISPLAY_NAME, BANK_BRANCHES);
    }
}

// Barclays requires account type selection first
class AccountTypeSelectionForm extends SelectionScenarioForm {
    private static final String FIELD_ID = "AccountType";
    private static final String FIELD_DISPLAY_NAME = "Select the account type";
    private static final List<Pair<String, String>> ACCOUNT_TYPES = List.of(
            Pair.of("barclays", "Barclays"),
            Pair.of("barclaycard", "Barclaycard"),
            Pair.of("business", "Business")
    );

    AccountTypeSelectionForm() {
        super(FIELD_ID, FIELD_DISPLAY_NAME, ACCOUNT_TYPES);
    }
}

// berlin flow could show a sca-selection
class SCASelectionForm extends SelectionScenarioForm {
    private static final String FIELD_ID = "scaMethod";
    private static final String FIELD_DISPLAY_NAME = "SCA Method";

    public ChallengeMethod getSelectedValue(FilledInUserSiteFormValues filledInUserSiteFormValues) {
        String selectedValue = filledInUserSiteFormValues.get(FIELD_ID);
        return ChallengeMethod.valueOf(selectedValue);
    }

    @RequiredArgsConstructor
    enum ChallengeMethod {
        SMS_OTP("SMS"), // sms
        CHIP_OTP_FLICKER("Chip OTP: flicker code"), // flickering barcodes
        CHIP_OTP_SIMPLE("Chip OTP: manual code"), // simply enter a challenge after following instructions
        PHOTO_OTP("Photo OTP (QR Code)"), // QR code
        PUSH_OTP("push message") // push  message
        ;
        final String displayName;
    }

    SCASelectionForm() {
        super(FIELD_ID, FIELD_DISPLAY_NAME, Arrays.stream(ChallengeMethod.values()).map(it -> Pair.of(it.name(), it.displayName)).toList());
    }
}

// CBC, KBC, 1822 Direkt, UniCredit RO require to input IBAN first
class IbanForm extends ScenarioForm {
    private static final String FIELD_ID = "Iban";

    @Override
    public Form buildForm() {
        // TextField, since the actual providers seem to use that instead of an actual IBAN field
        TextField ibanField = new TextField(FIELD_ID, "IBAN", 34, 34, false, false);

        return new Form(
                List.of(ibanField),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return formValues.get(FIELD_ID) != null;
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(FIELD_ID))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of(FIELD_ID, value));
    }
}

// Belfius requires to input IBAN and select the consent language first
class IbanAndLanguageForm extends ScenarioForm {
    private static final String IBAN_FIELD_ID = "Iban";
    private static final String LANGUAGE_FIELD_ID = "ConsentLanguage";
    private static final List<Pair<String, String>> LANGUAGES = List.of(
            Pair.of("nl", "nederlands"),
            Pair.of("fr", "française")
    );
    private static final List<String> LANGUAGE_CODES = LANGUAGES.stream()
            .map(Pair::getKey)
            .collect(Collectors.toList());

    @Override
    public Form buildForm() {
        // TextField, since the actual providers seem to use that instead of an actual IBAN field
        TextField ibanField = new TextField(IBAN_FIELD_ID, "IBAN", 34, 34, false, false);

        SelectField languageSelection = new SelectField(LANGUAGE_FIELD_ID, "Language", 0, 0, false, false);
        LANGUAGES.forEach(lang -> languageSelection.addSelectOptionValue(new SelectOptionValue(lang.getKey(), lang.getValue())));

        return new Form(
                List.of(languageSelection, ibanField),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return formValues.get(IBAN_FIELD_ID) != null &&
                                LANGUAGE_CODES.contains(formValues.get(LANGUAGE_FIELD_ID));
    }

    // Not sure about the purpose of this method.
    // Two pairs should be returned: IBAN and language. However, only one pair can be returned from this method.
    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(IBAN_FIELD_ID))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of("Iban", value));
    }
}

// Deutsche Bank (DE) requires to input branch and account number first
class BranchAndAccountForm extends ScenarioForm {
    private static final String BRANCH_FIELD_ID = "branch-number";
    private static final String ACCOUNT_FIELD_ID = "account-number";

    @Override
    public Form buildForm() {
        // TextField, since the actual providers seem to use that instead of an actual IBAN field
        TextField branchField = new TextField(BRANCH_FIELD_ID, "Branch number", 3, 3, false, false);
        TextField accountField = new TextField(ACCOUNT_FIELD_ID, "Account number", 7, 7, false, false);

        return new Form(
                List.of(branchField, accountField),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return formValues.get(BRANCH_FIELD_ID) != null &&
                                formValues.get(BRANCH_FIELD_ID).length() == 3 &&
                                formValues.get(ACCOUNT_FIELD_ID) != null &&
                                formValues.get(ACCOUNT_FIELD_ID).length() == 7;
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(ACCOUNT_FIELD_ID))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of(ACCOUNT_FIELD_ID, value));
    }
}

// Raiffeisen Bank (RO) requires to input iban and username first
class IbanAndUsernameForm extends ScenarioForm {
    private static final String IBAN_FIELD_ID = "Iban";
    private static final String USERNAME_FIELD_ID = "username";

    @Override
    public Form buildForm() {
        TextField ibanField = new TextField(IBAN_FIELD_ID, "IBAN", 34, 34, false, false);
        TextField accountField = new TextField(USERNAME_FIELD_ID, "Username", 34, 100, false, false);

        return new Form(
                List.of(ibanField, accountField),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return formValues.get(IBAN_FIELD_ID) != null && formValues.get(USERNAME_FIELD_ID) != null;
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.empty();
    }
}

// Deutsche Bank (ES) requires to input user name first
class UsernameForm extends ScenarioForm {
    private static final String FIELD_ID = "username";

    @Override
    public Form buildForm() {
        TextField usernameField = new TextField(FIELD_ID, "Username (self-defined or unique 2 to 59 alphanumeric characters)", 0, 59, false, false);

        return new Form(
                List.of(usernameField),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return formValues.get(FIELD_ID) != null && formValues.get(FIELD_ID).length() >= 2 && formValues.get(FIELD_ID).length() <= 59;
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(FIELD_ID))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of(FIELD_ID, value));
    }
}

// Deutsche Bank (IT) requires to input email first
class EmailForm extends ScenarioForm {
    private static final String FIELD_ID = "email";
    private static final String EMAIL_REGEX = "^\\S+@\\S+\\.\\S+$";
    public static TextField EMAIL_TEXT_FIELD = new TextField(FIELD_ID, "E-mail", 0, 200, false, false);

    @Override
    public Form buildForm() {

        return new Form(
                List.of(EMAIL_TEXT_FIELD),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return formValues.get(FIELD_ID) != null && Pattern.compile(EMAIL_REGEX).matcher(formValues.get(FIELD_ID)).matches();
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(FIELD_ID))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of(FIELD_ID, value));
    }
}

// Beobank requires to select consent language first
class LanguageSelectionForm extends SelectionScenarioForm {
    private static final String FIELD_ID = "ConsentLanguage";
    private static final String FIELD_DISPLAY_NAME = "Select consent language";
    private static final List<Pair<String, String>> LANGUAGE_CODES = List.of(
            Pair.of("nl", "nederlands"),
            Pair.of("fr", "française")
    );

    LanguageSelectionForm() {
        super(FIELD_ID, FIELD_DISPLAY_NAME, LANGUAGE_CODES);
    }
}

// BRD (RO) requires to input LoginID first
class LoginIdForm extends ScenarioForm {
    private static final String FIELD_ID = "LoginID";

    @Override
    public Form buildForm() {
        TextField field = new TextField(FIELD_ID, "Login ID", 0, 0, false, false);

        return new Form(
                List.of(field),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return StringUtils.isNotBlank(formValues.get(FIELD_ID));
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.ofNullable(formValues)
                .map(values -> values.get(FIELD_ID))
                .filter(StringUtils::isNotEmpty)
                .map(value -> Pair.of(FIELD_ID, value));
    }
}

//For embedded flow
@Slf4j
class UsernameAndPasswordForm extends ScenarioForm {
    private static final String USERNAME_FIELD_ID = "username";
    private static final String PASSWORD_FIELD_ID = "password";

    @Override
    public Form buildForm() {
        TextField usernameField = new TextField(USERNAME_FIELD_ID, "Username", 0, 2000, false, false);
        PasswordField passwordField = new PasswordField(PASSWORD_FIELD_ID, "Password", 0, 2000, false, null);

        return new Form(
                List.of(usernameField, passwordField),
                null,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        String userName = formValues.get(USERNAME_FIELD_ID);
        String password = formValues.get(PASSWORD_FIELD_ID);

        return !StringUtils.isBlank(userName) && !StringUtils.isBlank(password);
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.empty();
    }
}

@Slf4j
@RequiredArgsConstructor
class SimpleChallengeForm extends ScenarioForm {
    private static final String FIELD_ID = "challengeData";
    private final String explanationFieldExplanation;
    private final String textFieldDisplayName;

    @Override
    public Form buildForm() {
        TextField challengeField = new TextField(FIELD_ID, textFieldDisplayName, 0, 2000, false, false);
        var explanationField = new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", explanationFieldExplanation);

        return new Form(
                List.of(challengeField),
                explanationField,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return StringUtils.isNotBlank(formValues.get(FIELD_ID));
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.empty();
    }
}

@Slf4j
class FlickerChallengeForm extends ScenarioForm {
    private static final String FIELD_ID = "challengeData";
    private static final String FLICKER_CODE_ID = "flickerCode";

    @Override
    public Form buildForm() {
        TextField challengeField = new TextField(FIELD_ID, "flicker code response", 0, 2000, false, false);
        FlickerCodeField flickerCodeField = new FlickerCodeField(FLICKER_CODE_ID, "flicker code", "11048714955205123456789F14302C303107");
        var explanationField = new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "Please use your chip tan device to scan the code. Enter the response code.");

        return new Form(
                List.of(challengeField, flickerCodeField),
                explanationField,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return StringUtils.isNotBlank(formValues.get(FIELD_ID));
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.empty();
    }
}

/**
 * QR Code.
 */
@Slf4j
class PhotoOTPChallengeForm extends ScenarioForm {
    private static final String FIELD_ID = "challengeData";
    private static final String PHOTO_OTP_ID = "photoOtp";

    @Override
    public Form buildForm() {
        TextField challengeField = new TextField(FIELD_ID, "qr code response", 0, 2000, false, false);
        ImageField qrCodeField = new ImageField(PHOTO_OTP_ID, "QR-code", StubData.YOLT_SAMPLE_QR_CODE, "image/png");
        var explanationField = new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "Please use your mobile device to scan the QR code. Enter the response code.");

        return new Form(
                List.of(challengeField, qrCodeField),
                explanationField,
                null
        );
    }

    @Override
    public boolean isValid(FilledInUserSiteFormValues formValues) {
        return StringUtils.isNotBlank(formValues.get(FIELD_ID));
    }

    @Override
    public Optional<Pair<String, String>> extractQueryParameter(FilledInUserSiteFormValues formValues) {
        return Optional.empty();
    }
}
