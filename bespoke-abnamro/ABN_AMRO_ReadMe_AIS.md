## ABN Amro (AIS)

## BIP overview 
[Main reference BIP][1]

|                                       |                                                                                                                                                    |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | Netherlands                                                                                                                                        | 
| **Site Id**                           | 7670247e-323e-4275-82f6-87f31119dbd3                                                                                                               |
| **Standard**                          | BESPOKE                                                                                                                                            |
| **Contact**                           | E-mail: developer-support@nl.abnamro.com <br> Questions and requests have to be posted by ticket system:</br>https://developer.abnamro.com/support |
| **Developer Portal**                  | https://developer.abnamro.com                                                                                                                      |
| **Account SubTypes**                  | CURRENT_ACCOUNT                                                                                                                                    |
| **IP Whitelisting**                   |                                                                                                                                                    |
| **AIS Standard version**              | 1.1.1 (production)                                                                                                                                 |
| **Auto-onboarding**                   |                                                                                                                                                    |
| **Requires PSU IP address**           | No                                                                                                                                                 |
| **Type of certificate**               | eIDAS                                                                                                                                              |
| **Signing algorithms used**           |                                                                                                                                                    |
| **Mutual TLS Authentication Support** |                                                                                                                                                    |
| **Repository**                        | https://git.yolt.io/providers/bespoke-abnamro                                                                                                      |

## Client configuration overview
|                                  |                                                         |
|----------------------------------|---------------------------------------------------------|
| **Signing key id**               | Eidas signing key id                                    | 
| **Signing certificate**          | Eidas signing certificate                               | 
| **Transport key id**             | Eidas transport key id                                  |
| **Transport certificate**        | Eidas transport certificate                             |
| **Certificate Agreement Number** | value obtainable from our Eidas certificate, extensions |
| **API KEY**                      | provided from Abn Amro, no expiration date              | 

## Registration details
Adding the apps and products which you can register yourself via developer portal page are for testing with the sandbox
only. Please see link for explanation on developer portal: [Developer portal Basics][8]
If you would like to request access to production please see the following link [Request production access] [9]

## Multiple Registration
Bank statement: As a TPP you can have 1 production registration (1 client-id and 1 app in production/API-key) per certificate.

## Connection Overview
The swagger provided by the bank is broken and the original and corrected versions are inside swagger folder. Original [AIS] [3] and [PIS] [5] swagger, modified [AIS modified] [4] and [PIS modified] [5]
Minimal payment amount is 0.02 EUR. To check payment status you need to provide user token. This is currently not implemented as we cannot store user token in state and GetStatusRequest does not caontain any state.

**Consent validity rules**

Consent validity rules are provided both for AIS & PIS implementation.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business decisions
Currently none.

**Payment Flow Additional Information**

|                                                                                                        |                    |
|--------------------------------------------------------------------------------------------------------|--------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-consent |
| **it is possible to initiate a payment having no debtor account**                                      | NO                 |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | EXECUTED           |


## External links
* [Main reference BIP][1]
* [Documentation] [2]
* [Original AIS swagger] [3]
* [Modified AIS swagger] [4]
* [Original PIS swagger] [5]
* [Modified PIS swagger] [6]
* [Developer portal] [7]
* [Developer portal Basics] [8]
* [Request production access] [9]

[1]: 
[2]: https://developer.abnamro.com/api-products
[3]: ./swagger/abn-account-information-v1_original.yaml
[4]: ./swagger/abn-account-information-v1_2019-08-30.yaml
[5]: ./swagger/abn-pis.yaml
[6]: ./swagger/abn-pis-original.yaml
[7]: https://developer.abnamro.com/api-products
[8]: https://developer.abnamro.com/docs/basics
[9]: https://developer.abnamro.com/request-production-access
