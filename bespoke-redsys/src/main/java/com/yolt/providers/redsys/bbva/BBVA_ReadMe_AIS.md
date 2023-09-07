## BBVA (AIS)
[Current open problems on our end][1]

Banco Bilbao Vizcaya Argentaria, S.A., better known by its initialism BBVA, is a Spanish multinational financial services 
company based in Madrid and Bilbao, Spain. It is one of the largest financial institutions in the world, and is present 
mainly in Spain, South America, North America, Turkey and Romania. It is Spain's second largest bank after Banco Santander. 
It is listed on the Madrid Stock Exchange and is part of the IBEX 35
 
## BIP overview 
|                                       |                                                                                                                                                                                                                                                                                 |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | Spain                                                                                                                                                                                                                                                                           | 
| **Site Id**                           | 26c67b2b-91d3-491f-8121-b02fc9420101                                                                                                                                                                                                                                            |
| **Standard**                          | [Berlin Group Standard][2]                                                                                                                                                                                                                                                      |
| **Contact**                           | Email (production): psd2.hub.soporte@redsys.es / Email (sandbox) : psd2.sandbox.soporte@redsys.es .<br/> Email should contain [YOLT][{Bank name}] in its title.<br/> Attach filled Query_details.xlsx.<br/> If any video is sent remember to send it as compressed ZIP archive. |
| **Developer Portal**                  | https://market.apis-i.redsys.es/psd2/xs2a/nodos/bbva                                                                                                                                                                                                                            |
| **Account SubTypes**                  | Current                                                                                                                                                                                                                                                                         |
| **IP Whitelisting**                   | Bank is not supporting Whitelisting                                                                                                                                                                                                                                             |
| **AIS Standard version**              | 1.7.1                                                                                                                                                                                                                                                                           |
| **Auto-onboarding**                   | No                                                                                                                                                                                                                                                                              |
| **Requires PSU IP address**           | yes                                                                                                                                                                                                                                                                             |
| **Type of certificate**               | Eidas certificates are required : QWAC and QSEAL                                                                                                                                                                                                                                |
| **Signing algorithms used**           | RS256                                                                                                                                                                                                                                                                           |
| **Mutual TLS Authentication Support** | Yes                                                                                                                                                                                                                                                                             |
| **Repository**                        | https://git.yolt.io/providers/bespoke-redsys                                                                                                                                                                                                                                    |

## Links - sandbox
|                       |                                                                            |
|-----------------------|----------------------------------------------------------------------------|
| **Base url**          | https://apis-i.redsys.es:20443/psd2/xs2a/api-entrada-xs2a/services/BBVA    |
| **Authorization url** | https://apis-i.redsys.es:20443/psd2/xs2a/api-oauth-xs2a/services/rest/BBVA |

## Links - production 
|                       |                                                                                          |
|-----------------------|------------------------------------------------------------------------------------------|
| **Base url**          | https://psd2.redsys.es/api-entrada-xs2a/services/BBVA                                    |
| **Authorization url** | https://hubpsd2.redsys.es/api-oauth-xs2a/services/rest/BBVA                              |
| **Login domains**     | [psd2.apigateway.bbva](psd2.apigateway.bbva) <br> [hubpsd2.redsys.es](hubpsd2.redsys.es) |

## Client configuration overview
|                           |                                                       |
|---------------------------|-------------------------------------------------------|
| **Client id**             | Global Unique Reference Number from eIDAS certificate |
| **Transport certificate** | Eidas transport certificate                           |
| **Transport key id**      | Eidas transport key id                                |      
| **Signing certificate**   | Eidas signing certificate                             | 
| **Signing key id**        | Eidas signing key id                                  | 

## Registration details
In whole Redsys group there is no manual or dynamic registration. It is enough to use a valid eIDAS certificate (QWAC & QSEAL) 
with a unique organization identifier (GURN - Global Unique Reference Number) during mutual TLS authentication and signing the request.
The Global Unique Reference Number is used as _Client Id_.

## Connection Overview
Swagger can be downloaded from developer portal, but during implementation process we discovered that it is deprecated, 
so all model DTOs in our implementation were created manually based on documentation.

Documentation for the bank and swaggers can be found on their [developer portal][3]. To access those data you don't have 
to login using any credentials.

Redsys group has specific authorization flow. During `getLoginInfo` step user is redirected to consent page. Next during
first call of `createAccessMeans` user is redirected once again to bank site. This time SCA consent has to be filled.
After that we have _consentId_ value, which is required to fetch data.

Additionally `X-Request-ID` header has specific behaviour. It has to be different each call, except calls for transactions pages.
What is more in this group of banks there is `PSU-IP-Address` header required. It means that there is no limit for usage
of bank's api when call is triggered by user and  this header is present. For flywheel calls in some banks there are limits.
Due to that fact in whole group `BackPressureRequestException` exception is thrown when we receive _429_ error code in response
during fetch data process. From application perspective it means that this result should be skipped, so user doesn't know
that there was something wrong.

In BBVA there is no additional solution. Balances are retrieved using separate endpoint.

In Redsys group we have to pay attention for transactions fetching time. In BBVA there is no limit, so we collect data
based on value from _site-management_.  

Simplified sequence diagram:
```mermaid
sequenceDiagram
  autonumber
  participant PSU (via Yolt app);
  participant Yolt (providers);
  participant ASPSP;
  PSU (via Yolt app)->>+Yolt (providers): getLoginInfo()
  Yolt (providers)->>-PSU (via Yolt app): authorization URL (first consent page)
  PSU (via Yolt app)->>+ASPSP: authenticate on consent page
  ASPSP->>-PSU (via Yolt app): redirect to Yolt with authorization code
  PSU (via Yolt app)->>+Yolt (providers): createAccessMeans()
  Yolt (providers)->>+ASPSP: GET access token
  ASPSP->>-Yolt (providers): access token & refresh token
  Yolt (providers)->>+ASPSP: POST consent creation
  ASPSP->>-Yolt (providers): consent details (consentId)
  Yolt (providers)->>-PSU (via Yolt app): authorization URL (SCA consent page)
  PSU (via Yolt app)->>+ASPSP: authenticate on consent page
  ASPSP->>-PSU (via Yolt app): redirect to Yolt with authorization code
  PSU (via Yolt app)->>+Yolt (providers): createAccessMeans()
  Yolt (providers)-->>PSU (via Yolt app): create and store access mean
  PSU (via Yolt app)->>+Yolt (providers): fetchData()
  loop Various data fetches
  Yolt (providers)->>+ASPSP: GET accounts, balances and transactions
  ASPSP->>-Yolt (providers): accounts, balances and transactions list
  end
  Yolt (providers)-->>PSU (via Yolt app): user data

```

**Consent validity rules**

Consent validity rules are set to EMPTY_RULES_SET. We either get consent page, or we get error status.

## Sandbox overview
During implementation of whole group sandbox (Caixa Bank) was user. We were able to verify authorization API, but there was no possibility
to perform calls for AIS, because of requirement of eIDAS certificate for signature signing. 

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions
In Redsys group returned balance types depends on bank. Due to that fact we decided to prepare logic based on preferences
list. It means that as _Current Balance_ CLOSING_BOOKED, INTERIM_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED, EXPECTED types
are mapped in presented order. The same mechanism is implemented for _Available Balance_ and the order is as follows 
INTERIM_AVAILABLE, INTERIM_BOOKED, OPENING_BOOKED, CLOSING_BOOKED, EXPECTED. For BBVA only CLOSING_BOOKED and
INTERIM_AVAILABLE are returned.

Generic implementation is able to map both _pending_ and _booked_ transaction types and BBVA allows for both of them.

## Certificate rotation
Rotation of certificates is easy process. Due to the fact that there is no registration required, we can just update
authentication means on our side. Remember that certificates have to contain the same Global Unique Reference Number. 
Otherwise we also have to update _clientId_ authentication means and users will have to start from consent step, because 
from bank's perspective we will be treated as other TPP. 

**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-consent          |
| **it is possible to initiate a payment having no debtor account**                                      | YES                         |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |


## External links
* [Current open problems on our end][1]
* [Berlin Standard][2]
* [Developer portal][3]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20BBVA%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://www.berlin-group.org/>
[3]: <https://market.apis-i.redsys.es/psd2/xs2a/nodos/bbva>
