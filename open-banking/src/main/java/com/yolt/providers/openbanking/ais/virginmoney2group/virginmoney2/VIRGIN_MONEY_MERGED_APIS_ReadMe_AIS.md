## Virgin Money (Merged API) (AIS)
[Current open problems on our end][1]

## BIP overview

|                                       |                                                                                   |
|---------------------------------------|-----------------------------------------------------------------------------------|
| **Country of origin**                 | United Kingdom                                                                    |
| **Site Id**                           | 8a68103d-e277-4115-8283-d8db3e0c815d                                              |
| **Standard**                          | Open Banking                                                                      |
| **Contact**                           | E-mail: openbankingresponse@cybg.com                                              |
| **Developer Portal**                  | https://developer.virginmoney.com/merged-apis/                                    |
| **Account SubTypes**                  | Current, Savings ( Virgin products launched after 20.10.2019)                     |
| **IP Whitelisting**                   | No                                                                                |
| **AIS Standard version**              | v. 3.1.2                                                                          |
| **PISP Standard version**             | v. 3.1.2                                                                          |
| **Auto-onboarding**                   | Yes                                                                               |
| **Requires PSU IP address**           | No                                                                                |
| **Type of certificate**               | OBWAC/OBSEAL                                                                      |
| **Signing algorithms used**           | PS256                                                                             |
| **Mutual TLS Authentication Support** | Yes                                                                               |
| **Repository**                        | https://git.yolt.io/providers/open-banking                                        |

## Links - sandbox

|                         |                                                                                        |
|-------------------------|----------------------------------------------------------------------------------------|
| **Base URL**            | cb.sandbox-api-nc.cybservices.co.uk/open-banking/                                      |
| **Registration URL**    | cb.sandbox-api-nc.cybservices.co.uk/open-banking/v3.2/register                         |
| **Token Endpoint**      | cb.sandbox-api-nc.cybservices.co.uk/open-banking/v3.0/oauth2/token                     |
| **Well-known Endpoint** | cb.sandbox-api-nc.cybservices.co.uk/open-banking/v3.0/.well-known/openid-configuration |

## Links - production

|                         |                                                                                    |
|-------------------------|------------------------------------------------------------------------------------|
| **Base URL**            | api.openbanking.virginmoney.com/open-banking/                                      |
| **Registration URL**    | api.openbanking.virginmoney.com/open-banking/v3.2/register                         |
| **Token Endpoint**      | api.openbanking.virginmoney.com/open-banking/v3.0/oauth2/token                     |
| **Well-known Endpoint** | api.openbanking.virginmoney.com/open-banking/v3.0/.well-known/openid-configuration |

## Client configuration overview

|                                  |                                                        |
|----------------------------------|--------------------------------------------------------|
| **Signing key id**               | OBSEAL certificate id on our side (HSM)                |
| **Signing certificate**          | OBSEAL certificate                                     |
| **Transport key id**             | OBWAC certificate id on our side (HSM)                 |
| **Transport certificate**        | OBWAC certificate                                      |
| **Software Statement Assertion** | TPP's Open Banking Software Statement Assertion        |
| **Software id**                  | TPP's Open Banking software version                    |
| **Client id**                    | Unique identifier received during registration process |
| **Client secret**                | Unique secret received during registration process     | 

## Registration details

Bank supports DCR.

`token_endpoint_auth_methods` claim used upon DCR - `client_secret_basic`

## Connection Overview

Consent validity rules cannot be applied due to the fact VM2 consent page is SPA (Single Page Application).

## Sandbox overview

## User Site deletion

There's `onUserSiteDelete` method implemented by this provider, however, only in a best effort manner.

## Business and technical decisions

Bank **does not** support `ReadProducts` permission.

`fromBookingDateTime` query parameter for `/transactions` endpoint has to be in the following
format: `yyyy-MM-dd'T'HH:mm:ssxxx` (ex. `2021-01-01T13:34:54+00:00`).
What is more, it has to be URL encoded (percent encoded),
ex. `/transactions?fromBookingDateTime=2021-01-01T13%3A34%3A54%2B00%3A00`

C4PO-9893 SCA exemption. We will receive a Refresh Token when calling for an Access Token using grant_type::
authorization_code which can be used each time you call for a new Access Token using grant_type::refresh_token. You will
not receive a new refresh token each time you call for an Access Token when using grant_type::refresh_token.
This change will be live at 30.09.2022.

## External links

* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20VIRGIN_MONEY_MERGED_APIS%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>