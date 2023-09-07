## 1822 Direkt (AIS)
[Current open problems on our end][1]

## BIP overview

|                                       |                                                                                   |
|---------------------------------------|-----------------------------------------------------------------------------------|
| **Country of origin**                 | DE                                                                                | 
| **Site Id**                           | 83c792c0-f7f7-4714-b763-1a6522918228                                              |
| **Standard**                          | Berlin Group                                                                      |
| **Contact**                           | e-mail : interessent@1822direkt.de ( CONTACT IN GERMAN ONLY! )                    |
| **Developer Portal**                  | https://www.1822direkt.de/service/tan-verfahren-wechsel-psd2/psd2-for-developers/ | 
| **Account SubTypes**                  | Current (Checking Accounts ) , Savings with debit / credit card                   |
| **IP Whitelisting**                   | No                                                                                |
| **AIS Standard version**              | 1.2.1                                                                             |
| **Auto-onboarding**                   | no                                                                                |
| **Requires PSU IP address**           | yes                                                                               |
| **Type of certificate**               | eIDAS QWAC                                                                        |
| **Mutual TLS Authentication Support** |                                                                                   |
| **Repository**                        | https://git.yolt.io/providers/bespoke-1822direkt                                  |

## Links - sandbox

|                            |                                                                 |
|----------------------------|-----------------------------------------------------------------|
| **Base URL**               | https://sandbox.1822direkt-banking.de/joba-psd2/r1/v1/{service} | 
| **Authorization Endpoint** |                                                                 | 

## Links - production

|                             |                                                              |
|-----------------------------|--------------------------------------------------------------|
| **Base URL**                | https://xs2a.1822direkt-banking.de/joba-psd2/r1/v1/{service} | 
| **Authorization Endpoint**  | obtainable in rest call                                      |

## Client configuration overview
Example fields (for each bank fields might be different)

|                           |     |
|---------------------------|-----|
| **Transport key id**      |     |
| **Transport certificate** |     |

## Registration details

## Connection Overview

## Sandbox overview

## User Site deletion
There's `onUserSiteDelete` method implemented by this provider, however, only in a best effort manner.

## Business and technical decisions

**Consent validity rules**

1822Direkt AIS uses dynamic flow, thus we are unable to determine consent validity rules.

**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-consent          |
| **it is possible to initiate a payment having no debtor account**                                      | NO                          |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |

## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status%201822%20Direkt>
