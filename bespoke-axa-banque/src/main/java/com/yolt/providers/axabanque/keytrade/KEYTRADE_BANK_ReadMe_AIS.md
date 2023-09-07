## Keytrade Bank (AIS)
[Current open problems on our end][1]

Keytrade Bank is a financial services company based in Belgium with a subsidiary in Luxembourg.

Between 2005 and 2016, Keytrade Bank was part of the Crelan Group, which until 2015 was 50% owned by the Crédit Agricole Group, one of Europe's largest groups.
Since June 2016, Keytrade Bank has been part of the Crédit Mutuel Arkéa Group.

## BIP overview 

|                                       |                                                  |
|---------------------------------------|--------------------------------------------------|
| **Country of origin**                 | Belgium                                          |
| **Site Id**                           | 66bee776-2299-11ec-9621-0242ac130002             |
| **Standard**                          | Berlin Group [NextGenPSD2][2]                    |
| **Contact**                           | E-mail: payments@keytradebank.com                |
| **Developer Portal**                  | https://developer.keytradebank.be/               |
| **Account SubTypes**                  | Current                                          |
| **IP Whitelisting**                   | No                                               |
| **AIS Standard version**              | 6.1                                              |
| **Auto-onboarding**                   | No                                               |
| **Requires PSU IP address**           | No                                               |
| **Type of certificate**               | eIDAS QWAC                                       |
| **Signing algorithms used**           | no signing                                       |
| **Mutual TLS Authentication Support** | Yes                                              |
| **Repository**                        | https://git.yolt.io/providers/bespoke-axa-banque |

## Links - sandbox

|                       |                                                                 |
|-----------------------|-----------------------------------------------------------------|
| **Base URL**          | https://psd2.api.sandbox.keytradebank.be/berlingroup/v1/        |
| **Authorization URL** | https://psd2.api.sandbox.keytradebank.be/berlingroup/authorize/ | 
| **Token Endpoint**    | https://psd2.api.sandbox.keytradebank.be/berlingroup/v1/token   |  

## Links - production 

|                       |                                                         |
|-----------------------|---------------------------------------------------------|
| **Base URL**          | https://psd2.api.keytradebank.be/berlingroup/v1/        |
| **Authorization URL** | https://psd2.api.keytradebank.be/berlingroup/authorize/ | 
| **Token Endpoint**    | https://psd2.api.keytradebank.be/berlingroup/v1/token   |  

## Client configuration overview

|                           |                             |
|---------------------------|-----------------------------|
| **Transport key id**      | Eidas transport key id      |
| **Transport certificate** | Eidas transport certificate |

## Registration details
There is no registration.

## Multiple Registration

## Connection Overview
Connection is well described on bank's [developer portal][3].

## Sandbox overview
Sandbox is available but wasn't used during implementation.

## Consent validity rules
For now, it is impossible to implement validity rules as consent testing mechanism doesn't support client group. It will
be implemented when C4PO-10148 will be done.

The content of the consent page is loaded by means of JS file which makes it impossible to verify consent testing
by using keywords so it is better to keep EMPTY_RULES_SET property for Consent Validity Rules.

## User Site deletion
This provider implements `onUserSiteDelete` method.

## Business and technical decisions

## External links
* [Current open problems on our end][1]
* [NextGenPSD2][2]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20KEYTRADE%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://www.berlin-group.org/>
[3]: <https://developer.keytradebank.be/content/howto/ais-manage-consents>
