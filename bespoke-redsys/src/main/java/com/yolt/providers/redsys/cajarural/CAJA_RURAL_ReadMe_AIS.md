## Caja Rural (AIS)
[Current open problems on our end][1]

Caja Rural comprises around 35 cooperative banks and their undertakings.

 
## BIP overview 
|                                       |                                                                                                                                                                                                                             |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | Spain                                                                                                                                                                                                                       | 
| **Site Id**                           | 33f0fc98-d825-4d8b-952a-e4fe483f11f4                                                                                                                                                                                        |
| **Standard**                          | Berlin Group                                                                                                                                                                                                                |
| **Contact**                           | Email (production): psd2.hub.soporte@redsys.es <br/> Email should contain [YOLT][{Bank name}] in its title.<br/> Attach filled Query_details.xlsx.<br/> If any video is sent remember to send it as compressed ZIP archive. |
| **Developer Portal**                  | https://market.apis-i.redsys.es/psd2/xs2a/nodos/cajarural                                                                                                                                                                   |
| **Account SubTypes**                  | Current, Credits                                                                                                                                                                                                            |
| **IP Whitelisting**                   | No                                                                                                                                                                                                                          |
| **AIS Standard version**              | HUB-PSD2 1.0.0                                                                                                                                                                                                              |
| **Auto-onboarding**                   | No                                                                                                                                                                                                                          |
| **Requires PSU IP address**           |                                                                                                                                                                                                                             |
| **Type of certificate**               | eIDAS qWAC & qSeal Certificates are required                                                                                                                                                                                |
| **Signing algorithms used**           | RS256                                                                                                                                                                                                                       |
| **Mutual TLS Authentication Support** | Yes                                                                                                                                                                                                                         |
| **Repository**                        | https://git.yolt.io/providers/bespoke-bank-name                                                                                                                                                                             |


## Links - sandbox
|                            |                                                                           |
|----------------------------|---------------------------------------------------------------------------|
| **Base URL**               | https://apis-i.redsys.es:20443/psd2/xs2a/api-entrada-xs2a/services/BCE    |
| **Authorization Endpoint** | https://apis-i.redsys.es:20443/psd2/xs2a/api-oauth-xs2a/services/rest/BCE |

## Links - production 
|                            |                                                                      |
|----------------------------|----------------------------------------------------------------------|
| **Base URL**               | https://psd2.redsys.es/api-entrada-xs2a/services/eurocajarural       |
| **Authorization Endpoint** | https://hubpsd2.redsys.es/api-oauth-xs2a/services/rest/eurocajarural |

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
Swagger can be downloaded from developer portal, but during implementation it was discovered it varies from model that was manually created
and there was no reason not to use already existing model/

Documentation for the bank and swaggers can be found on their [developer portal][3]. To access those data you don't have 
to login using any credentials.

Redsys group has specific authorization flow. During `getLoginInfo` step user is redirected to consent page. Next during
first call of `createAccessMeans` user is redirected once again to bank site. This time SCA consent has to be filled.
After that we have _consentId_ value, which is required to fetch data.
In addition to that CajaRural has to choose Rural bank at first step.

Additionally `X-Request-ID` header has specific behaviour. It has to be different each call, except calls for transactions pages.
What is more in this group of banks there is `PSU-IP-Address` header required. It means that there is no limit for usage
of bank's api when call is triggered by user and  this header is present. For flywheel calls in some banks there are limits.
Due to that fact in whole group `BackPressureRequestException` exception is thrown when we receive _429_ error code in response
during fetch data process. From application perspective it means that this result should be skipped, so user doesn't know
that there was something wrong.

When fetching accounts  (_/accounts_ endpoint) parameter `withBalances` is used to get also
information about balances and reduce number of calls per day.

In Redsys group we have to pay attention for transactions fetching time. In Cajasur we are not aware of any limit so far, so we collect data
based on value from _site-management_.

Caja rural's consent process consists of 4 parts:
Obtaining rural bank user want's to connect to.
Generating authorization Url.
Generating consentId and authorizing it.
Generating authentication means.
What is unique for caja rural is that user has to choose in first flow step which rural bank to use.

**Consent validity rules**

It returns form on getLoginInfo so there is no point.

## Sandbox overview
Sandbox has not been used.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions
In Redsys group returned balance types depends on bank. Due to that fact we decided to prepare logic based on preferences
list. It means that as _Current Balance_ CLOSING_BOOKED, INTERIM_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED, EXPECTED types
are mapped in presented order. The same mechanism is implemented for _Available Balance_ and the order is as follows 
INTERIM_AVAILABLE, INTERIM_BOOKED, OPENING_BOOKED, CLOSING_BOOKED, EXPECTED.
CLOSINGBOOKED is returned.

Generic implementation is able to map both _pending_ and _booked_ transaction types.

There was a business decision confirmed with Product Owner to implement all of Rural Banks using dynamic flow option.
List of all regional bank's that are part of Caja Rural group is visible while entering https://market.apis-i.redsys.es/psd2/xs2a/nodos/cajarural and looking into the "Rural banks" section. 
In order to implement all of those you should use the unique CSB code that is generated for each of those banks.

## Certificate rotation
Rotation of certificates is easy process. Due to the fact that there is no registration required, we can just update
authentication means on our side. Remember that certificates have to contain the same Global Unique Reference Number. 
Otherwise we also have to update _clientId_ authentication means and users will have to start from consent step, because 
from bank's perspective we will be treated as other TPP. 
 
## External links
* [Current open problems on our end][1]
* [Developer portal][3]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22CAJA%20RURAL%22>
[3]: <https://market.apis-i.redsys.es/psd2/xs2a/nodos/cajarural>


