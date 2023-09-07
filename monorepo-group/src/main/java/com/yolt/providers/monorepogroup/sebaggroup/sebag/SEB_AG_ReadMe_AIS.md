## SEB AG (AIS)

[Current open problems on our end][1]

Skandinaviska Enskilda Banken AB, abbreviated SEB, is a northern European financial services group headquartered
in Stockholm, Sweden. In Sweden and the Baltic countries, SEB has a full financial service offering. In Denmark,
Finland, Norway, Germany, and the United Kingdom, the bank's operations are focused on corporate and investment
banking services to corporate and institutional clients. The bank was founded by the Swedish Wallenberg family, which
is still SEB's largest shareholder through investment company Investor AB.

## BIP overview

|                                       |                                                                     |
|---------------------------------------|---------------------------------------------------------------------|
| **Country of origin**                 | Germany                                                             | 
| **Site Id**                           | e59e2e59-efa4-4ae6-a9b8-7a3ec83be4d3                                |
| **Standard**                          | Custom                                                              |
| **Contact**                           | Ticketing system: https://developer.sebgroup.com/support/contact-us |
| **Developer Portal**                  | https://developer.sebgroup.com/home                                 | 
| **Account SubTypes**                  | Current, Credit (separate API) , Savings                            |
| **IP Whitelisting**                   | ?                                                                   |
| **AIS Standard version**              | 7.0.2 (AIS)                                                         |
| **Auto-onboarding**                   | NO                                                                  |
| **Requires PSU IP address**           | NO                                                                  |
| **Type of certificate**               | eIDAS QWAC                                                          |
| **Mutual TLS Authentication Support** | NO                                                                  |
| **Repository**                        | https://git.yolt.io/providers/monorepo-group/                       |

## Links - sandbox

|                       |                                                                  |
|-----------------------|------------------------------------------------------------------|
| **Base URL**          | https://api-sandbox.sebgroup.com/                                |
| **Authorization URL** | https://api-sandbox.sebgroup.com/mga/sps/oauth/oauth20/authorize | 
| **Token Endpoint**    | https://api-sandbox.sebgroup.com/mga/sps/oauth/oauth20/token     |  

## Links - production

|                            |                                                                   |
|----------------------------|-------------------------------------------------------------------|
| **Base URL**               | https://tpp-api.sebgroup.com/tpp/ais/v7/identified2/accounts      |
| **Authorization URL**      | https://id.seb.se/tpp                                             | 
| **Token Endpoint**         | https://tpp-api.sebgroup.com/mga/sps/oauth/oauth20/token          |

## Client configuration overview

|                              |                                    |
|------------------------------|------------------------------------|
| **Authentication mean name** | Authentication mean description    |
| **Client id**                | Client Identificator               |
| **Client secret**            | Client Secret                      |
| **Transport certificate**    | Eidas transport (QWAC) certificate |

## Registration details

Registration process is manual. One has to log in to the portal (standard credentials) , then choose Apps bookmark
and press the button "Request production access", a form to create new application will appear , after filling all
fields our Request will be checked by SEB AG and we will get a confirmation via e-mail together with the link to
activate the new app. NOTE: Remember to note client-secret during the app activation as it will be displayed only once.

## Multiple Registration

TBA

## Connection Overview

TBA

## Consent validity rules

TBA

## Business and technical decisions

## External links

* [Current open problems on our end][1]
* [Documentation][2]
* [Developer portal][3]
* [Sandbox access][4]
* [Ticketing system][5]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20SEB_AG%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>

[2]: <https://developer.sebgroup.com/products>

[3]: <https://developer.sebgroup.com/home>

[4]: <https://developer.sebgroup.com/products/psd2-account-information/account-information#sandbox>

[5]: <https://developer.sebgroup.com/support/contact-us>