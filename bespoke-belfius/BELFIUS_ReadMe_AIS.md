# Belfius
Useful links:
* [Developer portal](https://developer.belfius.be/devportal/en/getstarted/index.aspx)
    * [Consent spec](https://developer.belfius.be/devportal/en/apis/consent/index.aspx)
    * [AIS spec](https://developer.belfius.be/devportal/en/apis/accounts/index.aspx)
    * [PIS spec](https://developer.belfius.be/devportal/en/apis/payments/index.aspx)

|   |   |
|---|---|
| **Country of origin** | Belgium | 
| **Site Id**  |  61715853-e14c-46f3-b4ce-fdfe24cac95e |
| **Standard**   |  Bespoke |
| **AIS Standard version**  |  2 |
| **PISP Standard version**  |  - |
| **Account types**| CURRENT_ACCOUNT |


## Client configuration overview
|   |   |
|---|---|
| **Client id** | Was sent to us via email message| 
| **Client secret** | Was sent to us via email message| 
| **Client transport certificate** | Eidas transport certificate| 
| **Client transport private keyid** | Eidas transport key id| 
*PIS flow will also require signing certificate* 

### Registration details
Bank was register through email request. We sent them our organisation details, together with our QWAC and QSEAL
certificates. We receive the client id and secret in a email message secured with a password. 

## Connection Overview
Belfius connection can only be establish when user will provide a IBAN number.
We are using multi form for user to provide IBAN number and the consent language from the available list (fr, nl).
It is important to provide the consent language as if it will not be provided, Belfius will return list of authorization URLs in 
all available languages and we want only one, selected by the user. Additionally in the fetch flow there is a header parameter
`Accept-Language` that is required (missing or not supported, will cause 406 error), this parameter is filled with the
value that was selected by the user on the consent page.

`getLoginInfo` method is used for creating the specific form in order for user to provider IBAN and country.
`createNewAccessMeans` verifies if `urlCreateAccessMeans.getFilledInUserSiteFormValues()` are provided, if yes it means
the actual logic for `getLoginInfo` should be done. Otherwise the method creates access means as usual.

During the authorization flow Belfius is using PKCE (RFC https://tools.ietf.org/html/rfc7636).
When calling the `/consent-uris` endpoint we are sending `Code-Challenge` and `Code-Challenge-Method` parameters, and 
`code-verifier` is stored in `BelfiusGroupProviderState` object and used later to obtain access token.

`BelfiusGroupProviderState` object contains the `code-verifier` and the language that was selected by the user that will
later be used in fetch data step.

During the access and refresh step we are creating `BelfiusGroupAccessMeans` object that is saved as access means.
This object contains all the token information stored in `BelfiusGroupAccessToken` with the logical id that will be 
used to fetch the data (endpoints - `/accounts/{logical-id}` and `/accounts/{logical-id}/transactions`). Additionally
it contains `language` passed from `BelfiusGroupProviderState` and `redirectUrl` obtain from
`UrlCreateAccessMeansRequest`/`UrlRefreshAccessMeansRequest`. Both those parameters required in the fetch data flow.

As user is required to provide IBAN for the consent, we only fetch data (transactions, balance etc.) for that given account.

Balance is returned together with account details in the account request.

`psu_involved` is set to true in order to extend the limit calls for to more then 4. From documentation:
> **Retrieving balance and transactions when the PSU is not present** - 
  Four unattended calls can be perform on AIS APIs per 24 hours. In order to do so, the TPP should filled in the 
  “psu_involved” parameter with false. Note that if this parameter is empty, Belfius considers that the PSU is not present.
  This call will therefore be counted by Belfius as unattended.

Transactions are only fetch for 90 days, as the extended period requires special SCA-Token.
As Belfius described it in an email message (this is not yet implemented on our side):
>If our get transaction API is called to request transaction(s) older than 90 days using the dates_from and/or date_to
 parameters without a valid SCA-token, we will send you an 400 https status with error code 20007 “SCA_required”. 
 In the error_description, an url will be mentioned. 
 By calling this url (with a state), the PSU will be able to perform the required SCA in the Belfius environment. 
 After a successful SCA, the SCA-token will be send back. This SCA-token should be used in the header when calling the get transaction API again.


According to information received by email bank does support only current accounts 
> Based on the Belgian transposition of the Payment Services Directive, we consider a current account to fall within
  the description of a payment account. Credit and saving account are therefore not supported.

Additionally there is no additional possibility to fetch beneficiaries, standing orders direct debits etc. 

**Consent validity rules**
Belfius AIS uses dynamic flow, thus we are unable to determine consent validity rules for AIS.

### Mapping
Account response does not return any account ID reference, so we have decided to use the logical ID as according to 
swagger documentation:
> logical id mapped to IBAN (could be a UUID or even hashed IBAN)

Only one balance is returned from the bank. We are mapping it as current and available balance.

There is no information about transaction status so we are mapping it automatically as booked. 

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business decisions
Belfius returns just one balance type. A business decision was made with Leon to map this value to both available and current balances. 

## Sandbox overview
There is no validation of parameters for Sandbox environment, additional always static values are returned. 
  
**Payment Flow Additional Information**

|   |   |
|---|---|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?** | execute-on-consent |
| **it is possible to initiate a payment having no debtor account** | NO |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | RCVD status |

## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20Belfius%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: 
