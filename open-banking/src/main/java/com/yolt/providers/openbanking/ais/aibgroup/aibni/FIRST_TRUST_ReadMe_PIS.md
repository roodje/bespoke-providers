## AIB NI (PIS)
[Current open problems on our end][1]

AIB, part of AIB's UK subsidiary AIB Group plc, is a commercial bank in Northern Ireland. It forms part of one of the 
Big Four banks in Ireland. The bank was created in 1991 when TSB Northern Ireland merged with the AIB Group's 
other interests. Previously know in Yolt as First Trust Bank. First Trust Bank from the end of November 2019 became 
AIB, trading as AIB (NI).

## BIP overview

|                                       |                                                                                                                                                                                       |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | United Kingdom (Northern Ireland)                                                                                                                                                     | 
| **Site Id**                           | 9416de84-502b-4522-a622-635a02ed8924                                                                                                                                                  |
| **Standard**                          | [Open Banking Standard][2]                                                                                                                                                            |
| **Contact**                           | E-mail: api@aib.ie Ticketing system: https://openbanking.atlassian.net/servicedesk/customer/portal/1 |
| **Developer Portal**                  | https://developer.firsttrustbank.co.uk/                                                                                                                                               | 
| **Account SubTypes**                  | Current, Savings, Credit Cards                                                                                                                                                        |
| **IP Whitelisting**                   | No                                                                                                                                                                                    |
| **AIS Standard version**              | 3.1.4                                                                                                                                                                                 |
| **Auto-onboarding**                   | No                                                                                                                                                                                    |
| **Requires PSU IP address**           | No                                                                                                                                                                                    |
| **Type of certificate**               | OBSEAL/OBWAC                                                                                                                                                                          |
| **Signing algorithms used**           | PS256                                                                                                                                                                                 |
| **Mutual TLS Authentication Support** | Yes                                                                                                                                                                                   |
| **Repository**                        | https://git.yolt.io/providers/open-banking                                                                                                                                            |

## Links - sandbox

|                    |                                                                    |
|--------------------|--------------------------------------------------------------------|
| **Base URL**       | https://sandbox.firsttrustbank.co.uk/v2/sandbox/open-banking/v3.1/ |
| **Token Endpoint** | https://sandbox.firsttrustbank.co.uk/security/v2/oauth/token/      | 

## Links - production

|                       |                                                                 |
|-----------------------|-----------------------------------------------------------------|
| **Base URL**          | https://apis.firsttrustbank.co.uk/api/open-banking              |
| **Authorization URL** | https://onlinebanking.firsttrustbank.co.uk/inet/ft/tpplogin.htm | 
| **Token Endpoint**    | https://apis.firsttrustbank.co.uk/api/open-banking              | 

## Client configuration overview

|                                   |                                                                         |
|-----------------------------------|-------------------------------------------------------------------------|
| **Client id**                     | Unique identifier received during registration process                  | 
| **Client secret**                 | Secret value received during registration process                       | 
| **Institution id**                | Unique identifier of the financial institution assigned by Open Banking |
| **Private signing key header id** | Open Banking signing certificate key id                                 |
| **Signing key id**                | OBSEAL signing key id                                                   |
| **Transport key id**              | OBWAC transport key id                                                  |
| **Transport certificate**         | OBWAC transport certificate                                             |

## Registration details

The Bank has been registered manually in the AIB NI developer portal. Two applications were created - for Yolt PIS Test 
and YTS Unlicensed Clients. In order to register a new app in the portal you need to have a valid SSA , type a name
of the application and choose the scope - once the application is created it has to be approved by the AIB NI Support
Team, after that its fully operational.

## Multiple Registration

There is no information about any limit of applications that can be created in the developer portal.

## Connection Overview

AIB(NI) follow Open Banking standard. It means that flow is similar to other banks. Due to that fact,
Open Banking DTOs are used in implementation, and code relies mostly on our generic Open Banking implementation.
Bank allows payments in EUR currency and has been rewritten to use SEPA Payments (original implementation used 
UkDomesticPayments ).

## Sandbox overview

During implementation we didn't use sandbox, so we don't have knowledge about it.

## Business and technical decisions

02.03.2022 Due to agreements with yts-core it was decided that we want to treat payment as completed once money has been
deducted from debtor account. According to OB documentation `AcceptedSettlementCompleted` is proper status. For
reference see https://yolt.atlassian.net/browse/C4PO-9754

**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-submit           |
| **it is possible to initiate a payment having no debtor account**                                      | YES                         |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |

## External links
* [Current open problems on our end][1]
* [Open Banking Standard][2]
* [developer portal][3]
* [developer portal - supplementary-specification-information][4]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20C4PO%20AND%20component%20%3D%20AIB_NI%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>

[2]: <https://standards.openbanking.org.uk/>

[3]: <https://developer.firsttrustbank.co.uk//>

[4]: <https://developer.firsttrustbank.co.uk/supplementary-specification-information-ftb/>
